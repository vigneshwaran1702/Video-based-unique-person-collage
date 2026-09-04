package com.vignesh.uniquepersoncollage.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.data.model.ProcessingResult
import com.vignesh.uniquepersoncollage.data.repository.PipelineProgress
import com.vignesh.uniquepersoncollage.data.repository.ProcessingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProcessingUiState(
    val currentStageTitle: String = "Initializing...",
    val currentStageDetail: String = "Preparing pipeline",
    val progressFraction: Float = 0f,
    val framesProcessed: Int = 0,
    val totalFrames: Int = 0,
    val facesFound: Int = 0,
    val latestFaceBitmap: Bitmap? = null,
    val isCompleted: Boolean = false,
    val result: ProcessingResult? = null,
    val error: String? = null
)

class ProcessingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProcessingRepository(application)
    private var processingJob: Job? = null

    private val _uiState = MutableStateFlow(ProcessingUiState())
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    fun startProcessing(
        videoUri: Uri,
        sampleFps: Float,
        clusteringThreshold: Float,
        collageStyle: CollageStyle
    ) {
        processingJob?.cancel()
        _uiState.value = ProcessingUiState(currentStageTitle = "Starting video analysis...")

        processingJob = viewModelScope.launch {
            repository.processVideo(
                videoUri = videoUri,
                sampleFps = sampleFps,
                clusteringThreshold = clusteringThreshold,
                collageStyle = collageStyle
            ).collect { progress ->
                when (progress) {
                    is PipelineProgress.LoadingVideo -> {
                        _uiState.value = _uiState.value.copy(
                            currentStageTitle = "Loading Video",
                            currentStageDetail = progress.message,
                            progressFraction = 0.05f
                        )
                    }
                    is PipelineProgress.ExtractingAndDetecting -> {
                        val frac = 0.1f + (progress.currentFrame.toFloat() / progress.totalFrames.toFloat()) * 0.5f
                        _uiState.value = _uiState.value.copy(
                            currentStageTitle = "Frame Extraction & ML Kit Detection",
                            currentStageDetail = "Processing frame ${progress.currentFrame}/${progress.totalFrames}",
                            progressFraction = frac,
                            framesProcessed = progress.currentFrame,
                            totalFrames = progress.totalFrames,
                            facesFound = progress.facesFoundSoFar,
                            latestFaceBitmap = progress.latestFace ?: _uiState.value.latestFaceBitmap
                        )
                    }
                    is PipelineProgress.TrackingAndEmbedding -> {
                        val frac = 0.6f + (if (progress.totalTracks > 0) (progress.currentTrack.toFloat() / progress.totalTracks.toFloat()) * 0.2f else 0.2f)
                        _uiState.value = _uiState.value.copy(
                            currentStageTitle = "Tracking & TFLite Face Embedding",
                            currentStageDetail = "Extracting embeddings for track ${progress.currentTrack}/${progress.totalTracks}",
                            progressFraction = frac
                        )
                    }
                    is PipelineProgress.ClusteringIdentities -> {
                        _uiState.value = _uiState.value.copy(
                            currentStageTitle = "Identity Clustering",
                            currentStageDetail = progress.message,
                            progressFraction = 0.85f
                        )
                    }
                    is PipelineProgress.SelectingBestShots -> {
                        _uiState.value = _uiState.value.copy(
                            currentStageTitle = "Best Shot Selection",
                            currentStageDetail = progress.message,
                            progressFraction = 0.92f
                        )
                    }
                    is PipelineProgress.GeneratingCollage -> {
                        _uiState.value = _uiState.value.copy(
                            currentStageTitle = "Rendering Collage",
                            currentStageDetail = progress.message,
                            progressFraction = 0.98f
                        )
                    }
                    is PipelineProgress.Completed -> {
                        _uiState.value = _uiState.value.copy(
                            currentStageTitle = "Completed",
                            currentStageDetail = "Collage generated successfully!",
                            progressFraction = 1.0f,
                            isCompleted = true,
                            result = progress.result
                        )
                    }
                    is PipelineProgress.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = progress.message
                        )
                    }
                }
            }
        }
    }

    fun cancelProcessing() {
        processingJob?.cancel()
        _uiState.value = _uiState.value.copy(error = "Processing was cancelled.")
    }
}
