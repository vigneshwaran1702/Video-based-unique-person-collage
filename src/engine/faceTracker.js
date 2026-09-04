/**
 * Spatial-temporal Face Tracker. Associates detections across consecutive frames.
 */
export class FaceTracker {
  constructor() {
    this.tracks = [];
    this.nextTrackId = 1;
    this.maxTimeGap = 1.8; // seconds
  }

  processFrame(detections, timeSec) {
    const activeTracks = this.tracks.filter(t => (timeSec - t.lastTimestamp) <= this.maxTimeGap);
    const matchedDetections = new Set();

    for (const det of detections) {
      let bestTrack = null;
      let bestIoU = 0.25;

      for (const track of activeTracks) {
        const lastBox = track.lastBox;
        const iou = this.computeIoU(det.boundingBox, lastBox);
        if (iou > bestIoU) {
          bestIoU = iou;
          bestTrack = track;
        }
      }

      if (bestTrack) {
        bestTrack.detections.push(det);
        bestTrack.lastBox = det.boundingBox;
        bestTrack.lastTimestamp = timeSec;
        matchedDetections.add(det);
      }
    }

    for (const det of detections) {
      if (!matchedDetections.has(det)) {
        this.tracks.push({
          id: this.nextTrackId++,
          detections: [det],
          lastBox: det.boundingBox,
          startTime: timeSec,
          lastTimestamp: timeSec
        });
      }
    }
  }

  computeIoU(r1, r2) {
    const x1 = Math.max(r1.x, r2.x);
    const y1 = Math.max(r1.y, r2.y);
    const x2 = Math.min(r1.x + r1.width, r2.x + r2.width);
    const y2 = Math.min(r1.y + r1.height, r2.y + r2.height);

    const interArea = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
    const area1 = r1.width * r1.height;
    const area2 = r2.width * r2.height;
    const unionArea = area1 + area2 - interArea;

    return unionArea > 0 ? interArea / unionArea : 0;
  }

  getFinalTracks() {
    return this.tracks;
  }

  reset() {
    this.tracks = [];
    this.nextTrackId = 1;
  }
}
