package com.vignesh.uniquepersoncollage.data.model

import android.graphics.Bitmap

/**
 * Represents an appearance scene/interval of a person within the video.
 */
data class Appearance(
    val appearanceId: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val frameCount: Int,
    val bestShot: Bitmap?,
    val qualityScore: Float,
    val dominantPose: String = "Frontal"
) {
    val durationMs: Long
        get() = (endTimeMs - startTimeMs).coerceAtLeast(0L)
}
