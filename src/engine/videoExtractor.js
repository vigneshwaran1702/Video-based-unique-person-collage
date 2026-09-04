/**
 * Video metadata extraction and frame sampling using HTML5 Video and Canvas.
 */
export class VideoExtractor {
  constructor(videoElement) {
    this.video = videoElement;
  }

  async loadVideo(fileOrUrl) {
    return new Promise((resolve, reject) => {
      let url;
      let name = 'Demo Video';
      if (typeof fileOrUrl === 'string') {
        url = fileOrUrl;
      } else {
        url = URL.createObjectURL(fileOrUrl);
        name = fileOrUrl.name;
      }

      this.video.src = url;
      this.video.crossOrigin = 'anonymous';

      this.video.onloadedmetadata = () => {
        resolve({
          name,
          url,
          duration: this.video.duration,
          width: this.video.videoWidth,
          height: this.video.videoHeight,
          durationFormatted: this.formatTime(this.video.duration)
        });
      };

      this.video.onerror = (e) => {
        reject(new Error('Failed to load video format.'));
      };
    });
  }

  calculateSampleTimes(duration, fps = 2.5, maxFrames = 120) {
    const interval = 1 / fps;
    const timestamps = [];
    let cur = 0.1;
    while (cur < duration && timestamps.length < maxFrames) {
      timestamps.push(cur);
      cur += interval;
    }
    return timestamps;
  }

  async seekFrame(time) {
    return new Promise((resolve) => {
      const onSeeked = () => {
        this.video.removeEventListener('seeked', onSeeked);
        resolve(this.video);
      };
      this.video.addEventListener('seeked', onSeeked);
      this.video.currentTime = Math.min(time, this.video.duration - 0.05);
    });
  }

  captureFrame(canvas, ctx) {
    if (!canvas || !ctx) {
      canvas = document.createElement('canvas');
      ctx = canvas.getContext('2d');
    }
    canvas.width = this.video.videoWidth || 640;
    canvas.height = this.video.videoHeight || 360;
    ctx.drawImage(this.video, 0, 0, canvas.width, canvas.height);
    return { canvas, ctx };
  }

  formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }
}
