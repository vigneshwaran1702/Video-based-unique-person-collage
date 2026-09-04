package com.vignesh.uniquepersoncollage.collage

import android.graphics.Bitmap
import android.graphics.Rect
import com.vignesh.uniquepersoncollage.util.BitmapUtils

/**
 * Handles face framing, aspect ratio squaring, and portrait cropping.
 */
class ImageCropper {

    /**
     * Crops and aligns a face bitmap for collage tile placement.
     */
    fun prepareCollageTile(
        faceBitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        isCircular: Boolean = false,
        cornerRadiusPx: Float = 24f
    ): Bitmap {
        val scaled = Bitmap.createScaledBitmap(faceBitmap, targetWidth, targetHeight, true)
        return when {
            isCircular -> BitmapUtils.getCircularBitmap(scaled)
            cornerRadiusPx > 0f -> BitmapUtils.getRoundedCornerBitmap(scaled, cornerRadiusPx)
            else -> scaled
        }
    }
}
