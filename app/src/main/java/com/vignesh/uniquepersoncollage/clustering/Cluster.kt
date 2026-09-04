package com.vignesh.uniquepersoncollage.clustering

import com.vignesh.uniquepersoncollage.data.model.FaceEmbedding
import com.vignesh.uniquepersoncollage.data.model.FaceTrack
import com.vignesh.uniquepersoncollage.util.MathUtils

/**
 * Represents a cluster of face tracks recognized as the same unique human identity.
 */
data class Cluster(
    val clusterId: String,
    val tracks: MutableList<FaceTrack> = mutableListOf(),
    val embeddings: MutableList<FaceEmbedding> = mutableListOf(),
    var centroid: FaceEmbedding? = null
) {
    /**
     * Updates centroid embedding by calculating the normalized mean vector.
     */
    fun updateCentroid() {
        if (embeddings.isEmpty()) {
            centroid = null
            return
        }
        val vectors = embeddings.map { it.vector }
        val meanVector = MathUtils.computeCentroid(vectors)
        centroid = FaceEmbedding(meanVector)
    }

    /**
     * Merges another cluster into this cluster.
     */
    fun mergeWith(other: Cluster) {
        tracks.addAll(other.tracks)
        embeddings.addAll(other.embeddings)
        updateCentroid()
    }
}
