package com.vignesh.uniquepersoncollage.ml

import com.vignesh.uniquepersoncollage.data.model.FaceDetection
import com.vignesh.uniquepersoncollage.scoring.FaceQualityScore
import com.vignesh.uniquepersoncollage.scoring.SharpnessScorer
import com.vignesh.uniquepersoncollage.util.Constants
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Analyzes multiple quality metrics of a detected face to rank its suitability for collage generation.
 */
class FaceQualityAnalyzer {

    private val sharpnessScorer = SharpnessScorer()

    /**
     * Computes the composite FaceQualityScore for a detected face.
     */
    fun analyzeQuality(detection: FaceDetection, frameWidth: Int, frameHeight: Int): FaceQualityScore {
        // 1. Sharpness Score (0.0 to 1.0)
        val sharpness = detection.faceCropBitmap?.let {
            sharpnessScorer.calculateSharpness(it)
        } ?: 0.5f

        // 2. Frontal Pose Score (Penalty for excessive Yaw/Pitch/Roll)
        val absYaw = abs(detection.headEulerAngleY)
        val absPitch = abs(detection.headEulerAngleX)
        val absRoll = abs(detection.headEulerAngleZ)

        val yawFactor = max(0f, 1f - (absYaw / Constants.MAX_POSE_YAW_DEG))
        val pitchFactor = max(0f, 1f - (absPitch / Constants.MAX_POSE_PITCH_DEG))
        val rollFactor = max(0f, 1f - (absRoll / 30.0f))
        val poseScore = (yawFactor * 0.5f) + (pitchFactor * 0.35f) + (rollFactor * 0.15f)

        // 3. Resolution / Size Score relative to frame dimensions
        val faceArea = detection.boundingBox.width() * detection.boundingBox.height()
        val totalFrameArea = max(1, frameWidth * frameHeight)
        val areaRatio = faceArea.toFloat() / totalFrameArea.toFloat()
        // Normalized: optimal face is around 5% - 25% of frame area
        val sizeScore = min(1.0f, (areaRatio / 0.12f))

        // 4. Expression & Open Eyes Bonus
        var expressionScore = 0.5f
        if (detection.leftEyeOpenProbability != null && detection.rightEyeOpenProbability != null) {
            val eyesOpen = (detection.leftEyeOpenProbability + detection.rightEyeOpenProbability) / 2.0f
            expressionScore = eyesOpen
        }

        // Weighted Composite Score
        val composite = (sharpness * 0.35f) + (poseScore * 0.35f) + (sizeScore * 0.15f) + (expressionScore * 0.15f)

        return FaceQualityScore(
            compositeScore = composite.coerceIn(0f, 1f),
            sharpnessScore = sharpness,
            poseScore = poseScore,
            sizeScore = sizeScore,
            expressionScore = expressionScore
        )
    }
}
