package com.vignesh.uniquepersoncollage.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.vignesh.uniquepersoncollage.clustering.IdentityClusterer
import com.vignesh.uniquepersoncollage.collage.CollageGenerator
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.data.model.*
import com.vignesh.uniquepersoncollage.ml.FaceDetector
import com.vignesh.uniquepersoncollage.ml.FaceEmbedder
import com.vignesh.uniquepersoncollage.scoring.RepresentativeSelector
import com.vignesh.uniquepersoncollage.tracking.AppearanceSegmenter
import com.vignesh.uniquepersoncollage.tracking.FaceTracker
import com.vignesh.uniquepersoncollage.util.Constants
import com.vignesh.uniquepersoncollage.util.Logger
import com.vignesh.uniquepersoncollage.util.MathUtils
import com.vignesh.uniquepersoncollage.video.FrameSampler
import com.vignesh.uniquepersoncollage.video.VideoFrameExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID

/**
 * Pipeline progress states emitted during video processing.
 */
sealed class PipelineProgress {
    data class LoadingVideo(val message: String) : PipelineProgress()
    data class ExtractingAndDetecting(val currentFrame: Int, val totalFrames: Int, val facesFoundSoFar: Int, val latestFace: Bitmap? = null) : PipelineProgress()
    data class TrackingAndEmbedding(val currentTrack: Int, val totalTracks: Int) : PipelineProgress()
    data class ClusteringIdentities(val message: String) : PipelineProgress()
    data class SelectingBestShots(val message: String) : PipelineProgress()
    data class GeneratingCollage(val message: String) : PipelineProgress()
    data class Completed(val result: ProcessingResult) : PipelineProgress()
    data class Error(val message: String, val throwable: Throwable? = null) : PipelineProgress()
}

class ProcessingRepository(private val context: Context) {

    private val frameExtractor = VideoFrameExtractor(context)
    private val frameSampler = FrameSampler()
    private val faceDetector = FaceDetector()
    private val faceEmbedder = FaceEmbedder(context)
    private val faceTracker = FaceTracker()
    private val appearanceSegmenter = AppearanceSegmenter()
    private val identityClusterer = IdentityClusterer()
    private val representativeSelector = RepresentativeSelector()
    private val collageGenerator = CollageGenerator(context)

    /**
     * Executes the complete end-to-end processing pipeline as a Flow.
     */
    fun processVideo(
        videoUri: Uri,
        sampleFps: Float = Constants.DEFAULT_SAMPLE_FPS,
        clusteringThreshold: Float = Constants.DEFAULT_CLUSTERING_SIMILARITY_THRESHOLD,
        collageStyle: CollageStyle = CollageStyle.DYNAMIC_GRID
    ): Flow<PipelineProgress> = flow {
        val startTime = System.currentTimeMillis()

        // 1. Extract Video Info
        emit(PipelineProgress.LoadingVideo("Analyzing video metadata..."))
        val videoInfo = frameExtractor.extractVideoInfo(videoUri)
        if (videoInfo == null) {
            emit(PipelineProgress.Error("Could not read video file. Please select a valid video."))
            return@flow
        }

        // 2. Compute Sampling Timestamps
        val timestamps = frameSampler.calculateSampleTimestamps(
            durationMs = videoInfo.durationMs,
            targetFps = sampleFps
        )

        if (timestamps.isEmpty()) {
            emit(PipelineProgress.Error("Video has invalid duration or 0 frames."))
            return@flow
        }

        // 3. Extract Frames & Detect Faces
        faceTracker.reset()
        var totalFacesDetected = 0
        var latestThumbnail: Bitmap? = null

        for (index in timestamps.indices) {
            val ts = timestamps[index]
            val frameBitmap = frameExtractor.getFrameAtTime(videoUri, ts)

            if (frameBitmap != null) {
                val detections = faceDetector.detectFaces(frameBitmap, ts, index)
                if (detections.isNotEmpty()) {
                    totalFacesDetected += detections.size
                    latestThumbnail = detections.firstOrNull()?.faceCropBitmap ?: latestThumbnail
                    faceTracker.processFrame(detections, ts)
                }
            }

            emit(PipelineProgress.ExtractingAndDetecting(
                currentFrame = index + 1,
                totalFrames = timestamps.size,
                facesFoundSoFar = totalFacesDetected,
                latestFace = latestThumbnail
            ))
        }

        val allTracks = faceTracker.finalizeTracks()

        if (allTracks.isEmpty()) {
            val emptyResult = ProcessingResult(
                videoInfo = videoInfo,
                uniquePersons = emptyList(),
                totalFramesAnalyzed = timestamps.size,
                totalFacesDetected = 0,
                totalTracksCreated = 0,
                processingTimeMs = System.currentTimeMillis() - startTime,
                collageBitmap = collageGenerator.generateCollage(emptyList())
            )
            emit(PipelineProgress.Completed(emptyResult))
            return@flow
        }

        // 4. Compute Face Embeddings for each track
        emit(PipelineProgress.TrackingAndEmbedding(0, allTracks.size))
        for (i in allTracks.indices) {
            val track = allTracks[i]
            val embeddings = mutableListOf<FloatArray>()

            for (detection in track.detections) {
                detection.faceCropBitmap?.let { crop ->
                    val emb = faceEmbedder.getEmbedding(crop)
                    embeddings.add(emb.vector)
                }
            }

            if (embeddings.isNotEmpty()) {
                val meanVector = MathUtils.computeCentroid(embeddings)
                track.averageEmbedding = FaceEmbedding(meanVector)
            }

            emit(PipelineProgress.TrackingAndEmbedding(i + 1, allTracks.size))
        }

        // 5. Cluster Tracks into Unique Identities
        emit(PipelineProgress.ClusteringIdentities("Grouping face tracks into unique persons..."))
        val clusters = identityClusterer.clusterTracks(allTracks, clusteringThreshold)

        // 6. Select Best Shot and Build Persons
        emit(PipelineProgress.SelectingBestShots("Selecting highest quality face portraits..."))
        val persons = mutableListOf<Person>()

        for (i in clusters.indices) {
            val cluster = clusters[i]
            val clusterTracks = cluster.tracks

            // Best portrait shot
            val (bestDetection, bestScore) = representativeSelector.selectBestDetection(
                tracks = clusterTracks,
                videoWidth = videoInfo.width,
                videoHeight = videoInfo.height
            )

            // Discrete appearances
            val appearances = clusterTracks.map { track ->
                appearanceSegmenter.segmentTrackToAppearance(track)
            }

            val totalScreenTime = appearances.sumOf { it.durationMs }
            val thumbnails = representativeSelector.extractTopThumbnails(clusterTracks)

            val person = Person(
                id = UUID.randomUUID().toString(),
                name = "Person #${i + 1}",
                representativeFace = bestDetection?.faceCropBitmap,
                representativeScore = bestScore,
                appearanceCount = appearances.size,
                appearances = appearances,
                totalScreenTimeMs = totalScreenTime,
                representativeEmbedding = cluster.centroid,
                allFaceThumbnails = thumbnails
            )
            persons.add(person)
        }

        // Sort persons by appearance count / screen time descending
        val sortedPersons = persons.sortedByDescending { it.appearanceCount * 10000 + it.totalScreenTimeMs }
            .mapIndexed { index, person -> person.copy(name = "Person #${index + 1}") }

        // 7. Generate Artistic Collage
        emit(PipelineProgress.GeneratingCollage("Rendering high-resolution collage..."))
        val collageBitmap = collageGenerator.generateCollage(sortedPersons, collageStyle)

        val totalProcessingTime = System.currentTimeMillis() - startTime
        val finalResult = ProcessingResult(
            videoInfo = videoInfo,
            uniquePersons = sortedPersons,
            totalFramesAnalyzed = timestamps.size,
            totalFacesDetected = totalFacesDetected,
            totalTracksCreated = allTracks.size,
            processingTimeMs = totalProcessingTime,
            collageBitmap = collageBitmap
        )

        emit(PipelineProgress.Completed(finalResult))
    }.flowOn(Dispatchers.Default)

    /**
     * Regenerates the collage with a new layout style.
     */
    suspend fun regenerateCollage(
        persons: List<Person>,
        style: CollageStyle
    ): Bitmap {
        return collageGenerator.generateCollage(persons, style)
    }
}
