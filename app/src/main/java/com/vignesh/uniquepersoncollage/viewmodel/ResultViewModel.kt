package com.vignesh.uniquepersoncollage.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.data.model.Person
import com.vignesh.uniquepersoncollage.data.model.ProcessingResult
import com.vignesh.uniquepersoncollage.data.repository.ProcessingRepository
import com.vignesh.uniquepersoncollage.storage.GallerySaver
import com.vignesh.uniquepersoncollage.storage.ShareManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val result: ProcessingResult? = null,
    val currentStyle: CollageStyle = CollageStyle.DYNAMIC_GRID,
    val isRegenerating: Boolean = false,
    val isSaving: Boolean = false,
    val isSharing: Boolean = false,
    val saveSuccessUri: Uri? = null,
    val shareIntent: Intent? = null,
    val selectedPersonForDetail: Person? = null,
    val userMessage: String? = null
)

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProcessingRepository(application)
    private val gallerySaver = GallerySaver(application)
    private val shareManager = ShareManager(application)

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun setResult(result: ProcessingResult) {
        _uiState.value = _uiState.value.copy(result = result)
    }

    fun changeCollageStyle(newStyle: CollageStyle) {
        val currentResult = _uiState.value.result ?: return
        if (_uiState.value.currentStyle == newStyle) return

        _uiState.value = _uiState.value.copy(
            currentStyle = newStyle,
            isRegenerating = true
        )

        viewModelScope.launch {
            val newBitmap = repository.regenerateCollage(currentResult.uniquePersons, newStyle)
            _uiState.value = _uiState.value.copy(
                result = currentResult.copy(collageBitmap = newBitmap),
                isRegenerating = false
            )
        }
    }

    fun saveToGallery() {
        val bitmap = _uiState.value.result?.collageBitmap ?: return
        _uiState.value = _uiState.value.copy(isSaving = true, userMessage = null)

        viewModelScope.launch {
            val uri = gallerySaver.saveBitmapToGallery(bitmap)
            if (uri != null) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccessUri = uri,
                    userMessage = "Collage saved to Gallery successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    userMessage = "Failed to save collage to gallery."
                )
            }
        }
    }

    fun prepareShare() {
        val bitmap = _uiState.value.result?.collageBitmap ?: return
        _uiState.value = _uiState.value.copy(isSharing = true, shareIntent = null)

        viewModelScope.launch {
            val intent = shareManager.shareBitmap(bitmap)
            _uiState.value = _uiState.value.copy(
                isSharing = false,
                shareIntent = intent
            )
        }
    }

    fun selectPersonForDetail(person: Person?) {
        _uiState.value = _uiState.value.copy(selectedPersonForDetail = person)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }

    fun clearShareIntent() {
        _uiState.value = _uiState.value.copy(shareIntent = null)
    }
}
