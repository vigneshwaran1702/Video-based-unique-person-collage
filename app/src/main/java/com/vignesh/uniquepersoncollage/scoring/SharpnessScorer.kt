package com.vignesh.uniquepersoncollage.scoring

import android.graphics.Bitmap
import com.vignesh.uniquepersoncollage.util.BitmapUtils
import kotlin.math.min

/**
 * Evaluates image sharpness using variance of Laplacian / gradient energy on pixel luminance.
 */
class SharpnessScorer {

    /**
     * Calculates a normalized sharpness score in range [0.0, 1.0].
     */
    fun calculateSharpness(bitmap: Bitmap): Float {
        val scaled = BitmapUtils.resizeWithAspect(bitmap, 128)
        val w = scaled.width
        val h = scaled.height
        if (w < 3 || h < 3) return 0.5f

        val gray = BitmapUtils.toGrayscaleByteArray(scaled)

        var sum = 0.0
        var sumSq = 0.0
        var count = 0

        // 3x3 Laplacian Kernel:
        // [ 0,  1,  0 ]
        // [ 1, -4,  1 ]
        // [ 0,  1,  0 ]
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val center = (gray[y * w + x].toInt() and 0xFF)
                val top = (gray[(y - 1) * w + x].toInt() and 0xFF)
                val bottom = (gray[(y + 1) * w + x].toInt() and 0xFF)
                val left = (gray[y * w + (x - 1)].toInt() and 0xFF)
                val right = (gray[y * w + (x + 1)].toInt() and 0xFF)

                val laplacian = (top + bottom + left + right) - (4 * center)

                sum += laplacian
                sumSq += laplacian * laplacian
                count++
            }
        }

        if (count == 0) return 0.5f

        val mean = sum / count
        val variance = (sumSq / count) - (mean * mean)

        // Variance typically ranges from 10 to 500+ for faces
        // Normalize using sigmoid-like curve
        val normalized = (variance / (variance + 150.0)).toFloat()
        return min(1.0f, normalized)
    }
}
