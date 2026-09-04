package com.vignesh.uniquepersoncollage.data.model

import android.graphics.Bitmap

/**
 * Represents a unique person identified across the entire video.
 */
data class Person(
    val id: String,
    val name: String,
    val representativeFace: Bitmap?,
    val representativeScore: Float,
    val appearanceCount: Int,
    val appearances: List<Appearance> = emptyList(),
    val totalScreenTimeMs: Long = 0L,
    val representativeEmbedding: FaceEmbedding? = null,
    val allFaceThumbnails: List<Bitmap> = emptyList()
) {
    val screenTimeFormatted: String
        get() {
            val seconds = (totalScreenTimeMs / 1000) % 60
            val minutes = (totalScreenTimeMs / (1000 * 60)) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
