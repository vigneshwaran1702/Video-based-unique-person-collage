package com.vignesh.uniquepersoncollage.tracking

import android.graphics.Rect
import com.vignesh.uniquepersoncollage.data.model.FaceDetection
import com.vignesh.uniquepersoncollage.data.model.FaceTrack
import com.vignesh.uniquepersoncollage.util.Constants
import com.vignesh.uniquepersoncollage.util.MathUtils
import kotlin.math.hypot

/**
 * Matches detected faces in current frame with active face tracks.
 */
class TrackMatcher {

    /**
     * Matches new detections to active tracks based on spatial IoU and center distance.
     * Returns a map of Detection -> Matched FaceTrack.
     */
    fun matchDetectionsToTracks(
        detections: List<FaceDetection>,
        activeTracks: List<FaceTrack>,
        currentTimestampMs: Long
    ): Map<FaceDetection, FaceTrack> {
        val matches = mutableMapOf<FaceDetection, FaceTrack>()
        if (detections.isEmpty() || activeTracks.isEmpty()) return matches

        val availableTracks = activeTracks.filter { track ->
            (currentTimestampMs - track.lastTimestampMs) <= Constants.MAX_TRACK_GAP_MS
        }.toMutableList()

        for (detection in detections) {
            var bestTrack: FaceTrack? = null
            var bestScore = 0f

            for (track in availableTracks) {
                val lastBox = track.lastBoundingBox ?: continue
                val iou = MathUtils.calculateIoU(detection.boundingBox, lastBox)

                // If IoU is strong or center distance is very close
                val centerDistScore = calculateCenterProximity(detection.boundingBox, lastBox)
                val totalScore = (iou * 0.7f) + (centerDistScore * 0.3f)

                if (totalScore > Constants.IOU_TRACK_THRESHOLD && totalScore > bestScore) {
                    bestScore = totalScore
                    bestTrack = track
                }
            }

            if (bestTrack != null) {
                matches[detection] = bestTrack
                availableTracks.remove(bestTrack)
            }
        }

        return matches
    }

    private fun calculateCenterProximity(rect1: Rect, rect2: Rect): Float {
        val c1x = rect1.exactCenterX()
        val c1y = rect1.exactCenterY()
        val c2x = rect2.exactCenterX()
        val c2y = rect2.exactCenterY()

        val dist = hypot((c1x - c2x).toDouble(), (c1y - c2y).toDouble()).toFloat()
        val maxDim = (rect1.width() + rect2.width()) / 2.0f
        return (1.0f - (dist / (maxDim * 2.0f))).coerceIn(0f, 1f)
    }
}
