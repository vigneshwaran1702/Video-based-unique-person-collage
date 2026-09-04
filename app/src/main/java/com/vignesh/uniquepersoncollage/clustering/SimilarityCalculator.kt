package com.vignesh.uniquepersoncollage.clustering

import com.vignesh.uniquepersoncollage.data.model.FaceEmbedding
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min

/**
 * Provides similarity metrics and distance formulas for facial embeddings.
 */
object SimilarityCalculator {

    /**
     * Returns Cosine Similarity between two embeddings [-1.0, 1.0].
     */
    fun cosineSimilarity(emb1: FaceEmbedding, emb2: FaceEmbedding): Float {
        return emb1.cosineSimilarity(emb2)
    }

    /**
     * Returns Angular Similarity [0.0, 1.0] (1.0 = exact match).
     */
    fun angularSimilarity(emb1: FaceEmbedding, emb2: FaceEmbedding): Float {
        val cosSim = cosineSimilarity(emb1, emb2).coerceIn(-1f, 1f)
        val angleRad = acos(cosSim.toDouble())
        return (1.0 - (angleRad / Math.PI)).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Calculates average similarity between two clusters of embeddings.
     */
    fun averageLinkageSimilarity(
        clusterA: List<FaceEmbedding>,
        clusterB: List<FaceEmbedding>
    ): Float {
        if (clusterA.isEmpty() || clusterB.isEmpty()) return 0f
        var totalSim = 0.0
        var pairs = 0

        for (embA in clusterA) {
            for (embB in clusterB) {
                totalSim += cosineSimilarity(embA, embB)
                pairs++
            }
        }
        return if (pairs > 0) (totalSim / pairs).toFloat() else 0f
    }
}
