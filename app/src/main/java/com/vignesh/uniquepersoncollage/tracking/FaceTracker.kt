package com.vignesh.uniquepersoncollage.tracking

import com.vignesh.uniquepersoncollage.data.model.FaceDetection
import com.vignesh.uniquepersoncollage.data.model.FaceTrack
import java.util.concurrent.atomic.AtomicInteger

/**
 * Coordinates frame-by-frame face tracking and manages active track lifetimes.
 */
class FaceTracker {

    private val nextTrackId = AtomicInteger(1)
    private val activeTracks = mutableListOf<FaceTrack>()
    private val completedTracks = mutableListOf<FaceTrack>()
    private val trackMatcher = TrackMatcher()

    /**
     * Processes detections from a new frame.
     */
    fun processFrame(detections: List<FaceDetection>, timestampMs: Long) {
        // Match detections to active tracks
        val matches = trackMatcher.matchDetectionsToTracks(detections, activeTracks, timestampMs)

        val matchedDetections = mutableSetOf<FaceDetection>()

        for ((detection, track) in matches) {
            track.addDetection(detection)
            matchedDetections.add(detection)
        }

        // Unmatched detections start new tracks
        for (detection in detections) {
            if (!matchedDetections.contains(detection)) {
                val newTrack = FaceTrack(
                    trackId = nextTrackId.getAndIncrement(),
                    detections = mutableListOf(detection)
                )
                activeTracks.add(newTrack)
            }
        }
    }

    /**
     * Completes tracking session and returns all tracks.
     */
    fun finalizeTracks(): List<FaceTrack> {
        val allTracks = mutableListOf<FaceTrack>()
        allTracks.addAll(completedTracks)
        allTracks.addAll(activeTracks)
        return allTracks
    }

    fun reset() {
        activeTracks.clear()
        completedTracks.clear()
        nextTrackId.set(1)
    }
}
