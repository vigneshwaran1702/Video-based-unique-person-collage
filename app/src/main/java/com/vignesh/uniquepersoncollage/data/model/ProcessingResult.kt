package com.vignesh.uniquepersoncollage.data.model

import android.graphics.Bitmap
import android.net.Uri

/**
 * Result bundle after completing the entire video analysis and collage pipeline.
 */
data class ProcessingResult(
    val videoInfo: VideoInfo,
    val uniquePersons: List<Person>,
    val totalFramesAnalyzed: Int,
    val totalFacesDetected: Int,
    val totalTracksCreated: Int,
    val processingTimeMs: Long,
    val collageBitmap: Bitmap?,
    val savedCollageUri: Uri? = null
) {
    val personCount: Int
        get() = uniquePersons.size
}
