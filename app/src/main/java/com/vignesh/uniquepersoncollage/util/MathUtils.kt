package com.vignesh.uniquepersoncollage.util

import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object MathUtils {

    /**
     * Calculates Intersection over Union (IoU) of two bounding boxes.
     */
    fun calculateIoU(rect1: Rect, rect2: Rect): Float {
        val interLeft = max(rect1.left, rect2.left)
        val interTop = max(rect1.top, rect2.top)
        val interRight = min(rect1.right, rect2.right)
        val interBottom = min(rect1.bottom, rect2.bottom)

        val interWidth = max(0, interRight - interLeft)
        val interHeight = max(0, interBottom - interTop)
        val interArea = interWidth * interHeight

        val area1 = rect1.width() * rect1.height()
        val area2 = rect2.width() * rect2.height()

        val unionArea = area1 + area2 - interArea
        return if (unionArea > 0) interArea.toFloat() / unionArea.toFloat() else 0f
    }

    /**
     * Calculates L2 norm of a vector.
     */
    fun l2Norm(vector: FloatArray): Float {
        var sum = 0f
        for (v in vector) {
            sum += v * v
        }
        return sqrt(sum)
    }

    /**
     * Normalizes a float vector in-place or returns a new normalized vector.
     */
    fun normalizeL2(vector: FloatArray): FloatArray {
        val norm = l2Norm(vector)
        if (norm <= 1e-6f) return vector
        val result = FloatArray(vector.size)
        for (i in vector.indices) {
            result[i] = vector[i] / norm
        }
        return result
    }

    /**
     * Computes the mean vector (centroid) of a list of vectors.
     */
    fun computeCentroid(vectors: List<FloatArray>): FloatArray {
        if (vectors.isEmpty()) return FloatArray(0)
        val dim = vectors[0].size
        val centroid = FloatArray(dim)
        for (vec in vectors) {
            for (i in 0 until dim) {
                centroid[i] += vec[i]
            }
        }
        val count = vectors.size.toFloat()
        for (i in 0 until dim) {
            centroid[i] /= count
        }
        return normalizeL2(centroid)
    }
}
