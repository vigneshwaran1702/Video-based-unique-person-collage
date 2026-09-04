package com.vignesh.uniquepersoncollage.data.model

import android.net.Uri

/**
 * Metadata descriptor for an input video file.
 */
data class VideoInfo(
    val uri: Uri,
    val name: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val rotation: Int = 0,
    val fps: Float = 30.0f,
    val sizeBytes: Long = 0L,
    val mimeType: String = "video/mp4"
) {
    val durationFormatted: String
        get() {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = durationMs / (1000 * 60 * 60)
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val sizeFormatted: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return String.format("%.1f MB", mb)
        }
}
