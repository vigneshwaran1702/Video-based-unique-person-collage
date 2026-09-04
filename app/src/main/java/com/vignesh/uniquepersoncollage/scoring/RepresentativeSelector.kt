package com.vignesh.uniquepersoncollage.scoring

import android.graphics.Bitmap
import com.vignesh.uniquepersoncollage.data.model.FaceDetection
import com.vignesh.uniquepersoncollage.data.model.FaceTrack
import com.vignesh.uniquepersoncollage.ml.FaceQualityAnalyzer

/**
 * Selects the optimal portrait shot among multiple tracks and appearances belonging to an identity cluster.
 */
class RepresentativeSelector {

    private val qualityAnalyzer = FaceQualityAnalyzer()

    /**
     * Finds the highest quality detection from a list of face tracks.
     */
    fun selectBestDetection(
        tracks: List<FaceTrack>,
        videoWidth: Int,
        videoHeight: Int
    ): Pair<FaceDetection?, Float> {
        var bestDetection: FaceDetection? = null
        var maxScore = -1.0f

        for (track in tracks) {
            for (detection in track.detections) {
                if (detection.faceCropBitmap == null) continue

                val scoreObj = qualityAnalyzer.analyzeQuality(detection, videoWidth, videoHeight)
                if (scoreObj.compositeScore > maxScore) {
                    maxScore = scoreObj.compositeScore
                    bestDetection = detection
                }
            }
        }

        return Pair(bestDetection, maxScore.coerceAtLeast(0f))
    }

    /**
     * Extracts a list of top candidate thumbnails from tracks for gallery display.
     */
    fun extractTopThumbnails(
        tracks: List<FaceTrack>,
        maxCount: Int = 8
    ): List<Bitmap> {
        val crops = mutableListOf<Bitmap>()
        for (track in tracks) {
            for (det in track.detections) {
                det.faceCropBitmap?.let { crops.add(it) }
                if (crops.size >= maxCount * 2) break
            }
        }
        return crops.take(maxCount)
    }
}
