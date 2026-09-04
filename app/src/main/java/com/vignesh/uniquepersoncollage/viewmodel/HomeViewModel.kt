package com.vignesh.uniquepersoncollage.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.data.model.VideoInfo
import com.vignesh.uniquepersoncollage.util.Constants
import com.vignesh.uniquepersoncollage.video.VideoFrameExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedVideoUri: Uri? = null,
    val selectedVideoInfo: VideoInfo? = null,
    val sampleFps: Float = Constants.DEFAULT_SAMPLE_FPS,
    val clusteringThreshold: Float = Constants.DEFAULT_CLUSTERING_SIMILARITY_THRESHOLD,
    val selectedCollageStyle: CollageStyle = CollageStyle.DYNAMIC_GRID,
    val isLoadingInfo: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val frameExtractor = VideoFrameExtractor(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onVideoSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedVideoUri = uri,
            isLoadingInfo = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val info = frameExtractor.extractVideoInfo(uri)
            if (info != null) {
                _uiState.value = _uiState.value.copy(
                    selectedVideoInfo = info,
                    isLoadingInfo = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingInfo = false,
                    errorMessage = "Failed to load video metadata. Please try another video."
                )
            }
        }
    }

    fun updateFps(fps: Float) {
        _uiState.value = _uiState.value.copy(sampleFps = fps)
    }

    fun updateClusteringThreshold(threshold: Float) {
        _uiState.value = _uiState.value.copy(clusteringThreshold = threshold)
    }

    fun updateCollageStyle(style: CollageStyle) {
        _uiState.value = _uiState.value.copy(selectedCollageStyle = style)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
