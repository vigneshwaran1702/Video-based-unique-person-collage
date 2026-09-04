/**
 * Agglomerative Hierarchical Clustering for Identity grouping.
 */
export class IdentityClusterer {
  clusterTracks(tracks, threshold = 0.68) {
    if (tracks.length === 0) return [];

    // Initialize clusters
    const clusters = tracks.map((track, i) => ({
      clusterId: `person_${i + 1}`,
      tracks: [track],
      embeddings: [track.averageEmbedding],
      centroid: track.averageEmbedding
    }));

    let merged = true;
    while (merged && clusters.length > 1) {
      merged = false;
      let maxSim = -1;
      let bestPair = null;

      for (let i = 0; i < clusters.length; i++) {
        for (let j = i + 1; j < clusters.length; j++) {
          const sim = this.cosineSimilarity(clusters[i].centroid, clusters[j].centroid);
          if (sim > threshold && sim > maxSim) {
            maxSim = sim;
            bestPair = [i, j];
          }
        }
      }

      if (bestPair) {
        const [i, j] = bestPair;
        const cA = clusters[i];
        const cB = clusters[j];

        cA.tracks.push(...cB.tracks);
        cA.embeddings.push(...cB.embeddings);
        cA.centroid = this.computeCentroid(cA.embeddings);

        clusters.splice(j, 1);
        merged = true;
      }
    }

    return clusters;
  }

  computeCentroid(embeddings) {
    const dim = embeddings[0].length;
    const mean = new Float32Array(dim);
    for (const emb of embeddings) {
      for (let d = 0; d < dim; d++) {
        mean[d] += emb[d];
      }
    }
    const count = embeddings.length;
    for (let d = 0; d < dim; d++) {
      mean[d] /= count;
    }

    // Normalize
    let sumSq = 0;
    for (let d = 0; d < dim; d++) sumSq += mean[d] * mean[d];
    const norm = Math.sqrt(sumSq) || 1;
    for (let d = 0; d < dim; d++) mean[d] /= norm;

    return mean;
  }

  cosineSimilarity(v1, v2) {
    let dot = 0;
    for (let i = 0; i < v1.length; i++) {
      dot += v1[i] * v2[i];
    }
    return dot;
  }
}
