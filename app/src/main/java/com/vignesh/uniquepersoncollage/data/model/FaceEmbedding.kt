package com.vignesh.uniquepersoncollage.data.model

import kotlin.math.sqrt

/**
 * Represents a high-dimensional feature embedding vector representing a face.
 */
data class FaceEmbedding(
    val vector: FloatArray,
    val dimension: Int = vector.size
) {
    /**
     * Computes the Cosine Similarity between this embedding and another.
     * Value ranges from -1.0 to 1.0 (higher means more similar).
     */
    fun cosineSimilarity(other: FaceEmbedding): Float {
        if (this.dimension != other.dimension) return 0f

        var dotProduct = 0f
        var normA = 0f
        var normB = 0f

        for (i in vector.indices) {
            val a = vector[i]
            val b = other.vector[i]
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }

        val denominator = sqrt(normA) * sqrt(normB)
        return if (denominator > 1e-6f) dotProduct / denominator else 0f
    }

    /**
     * Computes Euclidean Distance between this embedding and another.
     * Lower means more similar.
     */
    fun euclideanDistance(other: FaceEmbedding): Float {
        if (this.dimension != other.dimension) return Float.MAX_VALUE

        var sumSq = 0f
        for (i in vector.indices) {
            val diff = vector[i] - other.vector[i]
            sumSq += diff * diff
        }
        return sqrt(sumSq)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FaceEmbedding
        return vector.contentEquals(other.vector)
    }

    override fun hashCode(): Int {
        return vector.contentHashCode()
    }
}
