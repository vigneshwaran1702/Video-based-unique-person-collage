package com.vignesh.uniquepersoncollage.scoring

/**
 * Breakdown of quality score factors for face shot selection.
 */
data class FaceQualityScore(
    val compositeScore: Float,
    val sharpnessScore: Float,
    val poseScore: Float,
    val sizeScore: Float,
    val expressionScore: Float
)
