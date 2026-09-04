package com.vignesh.uniquepersoncollage.ml

import android.graphics.Bitmap
import android.graphics.PointF
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.vignesh.uniquepersoncollage.data.model.FaceDetection as AppFaceDetection
import com.vignesh.uniquepersoncollage.util.BitmapUtils
import com.vignesh.uniquepersoncollage.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Google ML Kit Face Detection wrapper.
 */
class FaceDetector {

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.10f)
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(detectorOptions)

    /**
     * Detects faces in a frame bitmap asynchronously.
     */
    suspend fun detectFaces(
        bitmap: Bitmap,
        timestampMs: Long,
        frameIndex: Int
    ): List<AppFaceDetection> = withContext(Dispatchers.Default) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        try {
            val task = detector.process(inputImage)
            val mlKitFaces: List<Face> = Tasks.await(task)

            mlKitFaces.map { face ->
                val landmarks = mutableMapOf<Int, PointF>()
                face.allLandmarks.forEach { lm ->
                    landmarks[lm.landmarkType] = lm.position
                }

                val crop = BitmapUtils.cropFaceWithMargin(bitmap, face.boundingBox, 0.20f)

                AppFaceDetection(
                    frameTimestampMs = timestampMs,
                    boundingBox = face.boundingBox,
                    trackingId = face.trackingId,
                    headEulerAngleX = face.headEulerAngleX,
                    headEulerAngleY = face.headEulerAngleY,
                    headEulerAngleZ = face.headEulerAngleZ,
                    leftEyeOpenProbability = face.leftEyeOpenProbability,
                    rightEyeOpenProbability = face.rightEyeOpenProbability,
                    smilingProbability = face.smilingProbability,
                    landmarks = landmarks,
                    faceCropBitmap = crop,
                    frameIndex = frameIndex,
                    confidence = 1.0f
                )
            }
        } catch (e: Exception) {
            Logger.e("Face detection error at frame $frameIndex (time: ${timestampMs}ms)", e)
            emptyList()
        }
    }

    fun close() {
        try {
            detector.close()
        } catch (e: Exception) {
            Logger.e("Error closing FaceDetector", e)
        }
    }
}
