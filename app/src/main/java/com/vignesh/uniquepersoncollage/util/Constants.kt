package com.vignesh.uniquepersoncollage.util

object Constants {
    // Pipeline settings
    const val DEFAULT_SAMPLE_FPS = 2.0f
    const val MAX_VIDEO_FRAMES_TO_ANALYZE = 150
    const val MIN_FACE_SIZE_PX = 48

    // Face Quality thresholds
    const val MIN_QUALITY_SCORE = 0.25f
    const val MAX_POSE_YAW_DEG = 35.0f
    const val MAX_POSE_PITCH_DEG = 25.0f

    // Face Tracker parameters
    const val IOU_TRACK_THRESHOLD = 0.35f
    const val MAX_TRACK_GAP_MS = 1500L
    const val MIN_TRACK_FRAMES = 2

    // Identity Clustering parameters
    const val DEFAULT_CLUSTERING_SIMILARITY_THRESHOLD = 0.65f
    const val EMBEDDING_DIMENSION = 128

    // Collage dimensions
    const val COLLAGE_DEFAULT_WIDTH = 1920
    const val COLLAGE_DEFAULT_HEIGHT = 1080
}
