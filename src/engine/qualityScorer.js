/**
 * Quality Scoring & Best Portrait Selection.
 * Calculates Laplacian variance sharpness and pose symmetry.
 */
export class QualityScorer {
  calculateSharpness(cropCanvas) {
    const ctx = cropCanvas.getContext('2d');
    const w = cropCanvas.width;
    const h = cropCanvas.height;
    const data = ctx.getImageData(0, 0, w, h).data;

    let sum = 0, sumSq = 0, count = 0;
    // Fast Laplacian kernel approximation
    for (let y = 1; y < h - 1; y += 2) {
      for (let x = 1; x < w - 1; x += 2) {
        const c = (data[(y * w + x) * 4] + data[(y * w + x) * 4 + 1] + data[(y * w + x) * 4 + 2]) / 3;
        const top = (data[((y - 1) * w + x) * 4] + data[((y - 1) * w + x) * 4 + 1] + data[((y - 1) * w + x) * 4 + 2]) / 3;
        const btm = (data[((y + 1) * w + x) * 4] + data[((y + 1) * w + x) * 4 + 1] + data[((y + 1) * w + x) * 4 + 2]) / 3;
        const lft = (data[(y * w + (x - 1)) * 4] + data[(y * w + (x - 1)) * 4 + 1] + data[(y * w + (x - 1)) * 4 + 2]) / 3;
        const rgt = (data[(y * w + (x + 1)) * 4] + data[(y * w + (x + 1)) * 4 + 1] + data[(y * w + (x + 1)) * 4 + 2]) / 3;

        const lap = (top + btm + lft + rgt) - 4 * c;
        sum += lap;
        sumSq += lap * lap;
        count++;
      }
    }

    if (count === 0) return 0.5;
    const variance = (sumSq / count) - ((sum / count) ** 2);
    return Math.min(1.0, variance / (variance + 80));
  }

  selectBestShot(tracks) {
    let bestDetection = null;
    let maxScore = -1;

    for (const track of tracks) {
      for (const det of track.detections) {
        if (!det.cropCanvas) continue;

        const sharpness = this.calculateSharpness(det.cropCanvas);
        const poseScore = 1.0 - (Math.abs(det.yaw || 0) / 45);
        const composite = (sharpness * 0.6) + (poseScore * 0.4);

        if (composite > maxScore) {
          maxScore = composite;
          bestDetection = det;
        }
      }
    }

    return {
      bestDetection,
      qualityScore: Math.max(0, maxScore)
    };
  }
}
