package com.vignesh.uniquepersoncollage.clustering

import com.vignesh.uniquepersoncollage.data.model.FaceTrack
import com.vignesh.uniquepersoncollage.util.Constants
import java.util.UUID

/**
 * Agglomerative Hierarchical Clustering to group face tracks by human identity.
 */
class IdentityClusterer {

    /**
     * Clusters face tracks into unique human identities using cosine similarity thresholding.
     */
    fun clusterTracks(
        tracks: List<FaceTrack>,
        similarityThreshold: Float = Constants.DEFAULT_CLUSTERING_SIMILARITY_THRESHOLD
    ): List<Cluster> {
        val validTracks = tracks.filter { it.averageEmbedding != null && it.detections.isNotEmpty() }
        if (validTracks.isEmpty()) return emptyList()

        // 1. Initialize each track as its own individual cluster
        val clusters = validTracks.map { track ->
            val cluster = Cluster(clusterId = UUID.randomUUID().toString())
            cluster.tracks.add(track)
            track.averageEmbedding?.let { cluster.embeddings.add(it) }
            cluster.updateCentroid()
            cluster
        }.toMutableList()

        // 2. Iteratively merge closest pairs if similarity exceeds threshold
        var merged = true
        while (merged && clusters.size > 1) {
            merged = false
            var bestSim = -1.0f
            var bestPair: Pair<Int, Int>? = null

            for (i in 0 until clusters.size) {
                val cA = clusters[i]
                val centA = cA.centroid ?: continue

                for (j in (i + 1) until clusters.size) {
                    val cB = clusters[j]
                    val centB = cB.centroid ?: continue

                    val sim = SimilarityCalculator.cosineSimilarity(centA, centB)
                    if (sim > similarityThreshold && sim > bestSim) {
                        bestSim = sim
                        bestPair = Pair(i, j)
                    }
                }
            }

            bestPair?.let { (i, j) ->
                val clusterA = clusters[i]
                val clusterB = clusters[j]
                clusterA.mergeWith(clusterB)
                clusters.removeAt(j)
                merged = true
            }
        }

        return clusters
    }
}
