package com.vignesh.uniquepersoncollage.video

import com.vignesh.uniquepersoncollage.util.Constants
import kotlin.math.max

/**
 * Calculates sampling timestamps for video processing.
 */
class FrameSampler {

    /**
     * Generates a list of frame timestamps in milliseconds to sample from the video.
     */
    fun calculateSampleTimestamps(
        durationMs: Long,
        targetFps: Float = Constants.DEFAULT_SAMPLE_FPS,
        maxFrames: Int = Constants.MAX_VIDEO_FRAMES_TO_ANALYZE
    ): List<Long> {
        if (durationMs <= 0) return emptyList()

        val intervalMs = (1000.0f / targetFps).toLong().coerceAtLeast(100L)
        val estimatedCount = (durationMs / intervalMs).toInt()

        val effectiveIntervalMs = if (estimatedCount > maxFrames) {
            durationMs / maxFrames
        } else {
            intervalMs
        }

        val timestamps = mutableListOf<Long>()
        var current = 0L
        while (current < durationMs && timestamps.size < maxFrames) {
            timestamps.add(current)
            current += max(100L, effectiveIntervalMs)
        }

        return timestamps
    }
}
