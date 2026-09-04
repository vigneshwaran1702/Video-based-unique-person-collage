package com.vignesh.uniquepersoncollage.data.model

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect

/**
 * Represents a face detected within a specific video frame.
 */
data class FaceDetection(
    val frameTimestampMs: Long,
    val boundingBox: Rect,
    val trackingId: Int? = null,
    val headEulerAngleX: Float = 0f, // Pitch: Up/Down (- is looking down, + is looking up)
    val headEulerAngleY: Float = 0f, // Yaw: Left/Right (- is looking left, + is looking right)
    val headEulerAngleZ: Float = 0f, // Roll: Tilt sideways
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val smilingProbability: Float? = null,
    val landmarks: Map<Int, PointF> = emptyMap(),
    val faceCropBitmap: Bitmap? = null,
    val frameIndex: Int = 0,
    val confidence: Float = 1.0f
)
