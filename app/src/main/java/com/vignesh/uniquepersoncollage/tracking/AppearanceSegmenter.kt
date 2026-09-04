package com.vignesh.uniquepersoncollage.tracking

import com.vignesh.uniquepersoncollage.data.model.Appearance
import com.vignesh.uniquepersoncollage.data.model.FaceTrack
import java.util.UUID

/**
 * Converts tracked face sequences into discrete temporal appearance segments.
 */
class AppearanceSegmenter {

    /**
     * Converts a single face track into an Appearance domain model.
     */
    fun segmentTrackToAppearance(track: FaceTrack): Appearance {
        val dominantPose = if (track.detections.isNotEmpty()) {
            val avgYaw = track.detections.map { it.headEulerAngleY }.average()
            when {
                avgYaw > 15.0 -> "Looking Right"
                avgYaw < -15.0 -> "Looking Left"
                else -> "Frontal"
            }
        } else {
            "Frontal"
        }

        return Appearance(
            appearanceId = UUID.randomUUID().toString(),
            startTimeMs = track.startTimeMs,
            endTimeMs = track.endTimeMs,
            frameCount = track.detections.size,
            bestShot = track.bestDetection?.faceCropBitmap,
            qualityScore = track.bestQualityScore,
            dominantPose = dominantPose
        )
    }
}
