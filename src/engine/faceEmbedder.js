/**
 * Facial Feature Embedding Extractor.
 * Extracts a normalized 64/128-dimensional spatial-color feature vector from face crops.
 */
export class FaceEmbedder {
  constructor() {
    this.dim = 64;
  }

  extractEmbedding(cropCanvas) {
    const ctx = cropCanvas.getContext('2d');
    const w = cropCanvas.width;
    const h = cropCanvas.height;
    const imgData = ctx.getImageData(0, 0, w, h).data;

    const vector = new Float32Array(this.dim);
    const gridSize = 4;
    const cellW = Math.floor(w / gridSize);
    const cellH = Math.floor(h / gridSize);

    let vIdx = 0;
    for (let gy = 0; gy < gridSize; gy++) {
      for (let gx = 0; gx < gridSize; gx++) {
        let rSum = 0, gSum = 0, bSum = 0, count = 0;
        for (let y = gy * cellH; y < (gy + 1) * cellH; y++) {
          for (let x = gx * cellW; x < (gx + 1) * cellW; x++) {
            const idx = (y * w + x) * 4;
            rSum += imgData[idx];
            gSum += imgData[idx + 1];
            bSum += imgData[idx + 2];
            count++;
          }
        }
        const avgR = rSum / (count * 255);
        const avgG = gSum / (count * 255);
        const avgB = bSum / (count * 255);

        vector[vIdx++] = avgR - avgG;
        vector[vIdx++] = avgG - avgB;
        vector[vIdx++] = avgR - avgB;
        vector[vIdx++] = (avgR + avgG + avgB) / 3;
      }
    }

    return this.normalizeL2(vector);
  }

  normalizeL2(vec) {
    let sumSq = 0;
    for (let i = 0; i < vec.length; i++) {
      sumSq += vec[i] * vec[i];
    }
    const norm = Math.sqrt(sumSq) || 1e-6;
    const normalized = new Float32Array(vec.length);
    for (let i = 0; i < vec.length; i++) {
      normalized[i] = vec[i] / norm;
    }
    return normalized;
  }

  cosineSimilarity(v1, v2) {
    let dot = 0;
    for (let i = 0; i < v1.length; i++) {
      dot += v1[i] * v2[i];
    }
    return Math.max(-1, Math.min(1, dot));
  }
}
