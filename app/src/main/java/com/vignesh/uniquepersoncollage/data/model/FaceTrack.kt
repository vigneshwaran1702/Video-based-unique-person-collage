package com.vignesh.uniquepersoncollage.data.model

import android.graphics.Rect

/**
 * Represents a sequence of continuous face detections belonging to one track in the video.
 */
data class FaceTrack(
    val trackId: Int,
    val detections: MutableList<FaceDetection> = mutableListOf(),
    var averageEmbedding: FaceEmbedding? = null,
    var bestDetection: FaceDetection? = null,
    var bestQualityScore: Float = 0f,
    val startTimeMs: Long = detections.firstOrNull()?.frameTimestampMs ?: 0L,
    var endTimeMs: Long = detections.lastOrNull()?.frameTimestampMs ?: 0L
) {
    val durationMs: Long
        get() = (endTimeMs - startTimeMs).coerceAtLeast(0L)

    val lastBoundingBox: Rect?
        get() = detections.lastOrNull()?.boundingBox

    val lastTimestampMs: Long
        get() = detections.lastOrNull()?.frameTimestampMs ?: 0L

    fun addDetection(detection: FaceDetection) {
        detections.add(detection)
        endTimeMs = detection.frameTimestampMs
    }
}
