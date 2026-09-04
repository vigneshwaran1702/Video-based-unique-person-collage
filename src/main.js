import { VideoExtractor } from './engine/videoExtractor.js';
import { FaceDetector } from './engine/faceDetector.js';
import { FaceTracker } from './engine/faceTracker.js';
import { FaceEmbedder } from './engine/faceEmbedder.js';
import { IdentityClusterer } from './engine/identityClusterer.js';
import { QualityScorer } from './engine/qualityScorer.js';
import { CollageRenderer } from './engine/collageRenderer.js';
import confetti from 'canvas-confetti';

// DOM Elements
const hiddenVideo = document.getElementById('hiddenVideo');
const dropzone = document.getElementById('videoDropzone');
const fileInput = document.getElementById('videoFileInput');
const videoMetaCard = document.getElementById('videoMetaCard');
const metaFileName = document.getElementById('metaFileName');
const metaDuration = document.getElementById('metaDuration');
const metaDimensions = document.getElementById('metaDimensions');

const fpsSlider = document.getElementById('samplingFpsSlider');
const fpsVal = document.getElementById('fpsVal');
const threshSlider = document.getElementById('clusteringThreshSlider');
const threshVal = document.getElementById('threshVal');
const styleButtons = document.querySelectorAll('.style-btn');
const btnProcessVideo = document.getElementById('btnProcessVideo');

const welcomePlaceholder = document.getElementById('welcomePlaceholder');
const pipelineCard = document.getElementById('pipelineCard');
const resultStudio = document.getElementById('resultStudio');

const stageTitle = document.getElementById('stageTitle');
const stageSubtitle = document.getElementById('stageSubtitle');
const statFrames = document.getElementById('statFrames');
const statFaces = document.getElementById('statFaces');
const statTracks = document.getElementById('statTracks');
const progressBar = document.getElementById('progressBar');
const liveCanvas = document.getElementById('liveCanvas');
const liveCtx = liveCanvas.getContext('2d');
const liveFacesFeed = document.getElementById('liveFacesFeed');
const collageCanvas = document.getElementById('collageCanvas');
const personsList = document.getElementById('personsList');
const resultSummaryText = document.getElementById('resultSummaryText');
const btnDownloadCollage = document.getElementById('btnDownloadCollage');
const btnShareCollage = document.getElementById('btnShareCollage');

// Demo Buttons
const btnDemoInterview = document.getElementById('btnDemoInterview');
const btnDemoFriends = document.getElementById('btnDemoFriends');

// Engine instances
const videoExtractor = new VideoExtractor(hiddenVideo);
const faceDetector = new FaceDetector();
const faceTracker = new FaceTracker();
const faceEmbedder = new FaceEmbedder();
const identityClusterer = new IdentityClusterer();
const qualityScorer = new QualityScorer();
const collageRenderer = new CollageRenderer();

// State
let selectedVideoFile = null;
let currentVideoInfo = null;
let selectedStyle = 'DYNAMIC_GRID';
let currentPersonsResult = [];

// Event Listeners for UI controls
fpsSlider.addEventListener('input', (e) => {
  fpsVal.textContent = `${parseFloat(e.target.value).toFixed(1)} FPS`;
});

threshSlider.addEventListener('input', (e) => {
  threshVal.textContent = parseFloat(e.target.value).toFixed(2);
});

styleButtons.forEach((btn) => {
  btn.addEventListener('click', () => {
    styleButtons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    selectedStyle = btn.dataset.style;
    if (currentPersonsResult.length > 0) {
      collageRenderer.renderCollage(collageCanvas, currentPersonsResult, selectedStyle);
    }
  });
});

// Drag & Drop
dropzone.addEventListener('click', () => fileInput.click());
dropzone.addEventListener('dragover', (e) => {
  e.preventDefault();
  dropzone.classList.add('dragover');
});
dropzone.addEventListener('dragleave', () => dropzone.classList.remove('dragover'));
dropzone.addEventListener('drop', (e) => {
  e.preventDefault();
  dropzone.classList.remove('dragover');
  if (e.dataTransfer.files.length > 0) {
    handleVideoSelected(e.dataTransfer.files[0]);
  }
});

fileInput.addEventListener('change', (e) => {
  if (e.target.files.length > 0) {
    handleVideoSelected(e.target.files[0]);
  }
});

// Demo video generator (Generates animated synthetic multi-person sample in canvas/blob)
btnDemoInterview.addEventListener('click', () => createDemoVideo(3, 'Tech Panel Interview'));
btnDemoFriends.addEventListener('click', () => createDemoVideo(5, 'Birthday Celebration'));

async function handleVideoSelected(file) {
  selectedVideoFile = file;
  try {
    const info = await videoExtractor.loadVideo(file);
    currentVideoInfo = info;
    metaFileName.textContent = info.name;
    metaDuration.textContent = info.durationFormatted;
    metaDimensions.textContent = `${info.width} × ${info.height}`;
    videoMetaCard.classList.remove('hidden');
    btnProcessVideo.removeAttribute('disabled');
  } catch (err) {
    alert('Failed to load video. Please select a valid video file.');
  }
}

// Generate an animated sample video stream for instant testing
async function createDemoVideo(personCount, title) {
  const demoCanvas = document.createElement('canvas');
  demoCanvas.width = 640;
  demoCanvas.height = 360;
  const ctx = demoCanvas.getContext('2d');

  const stream = demoCanvas.captureStream(30);
  const mediaRecorder = new MediaRecorder(stream, { mimeType: 'video/webm' });
  const chunks = [];
  mediaRecorder.ondataavailable = (e) => chunks.push(e.data);

  mediaRecorder.start();

  const colors = ['#f43f5e', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b'];
  const names = ['Alex', 'Sarah', 'David', 'Elena', 'Michael'];

  const totalFrames = 60;
  for (let f = 0; f < totalFrames; f++) {
    // Background scene
    ctx.fillStyle = '#1e1b4b';
    ctx.fillRect(0, 0, 640, 360);

    // Draw title
    ctx.fillStyle = 'white';
    ctx.font = 'bold 20px sans-serif';
    ctx.fillText(`${title} (Demo)`, 20, 40);

    // Draw simulated persons with moving head portraits
    for (let p = 0; p < personCount; p++) {
      const active = (f + p * 15) % 40 < 30; // comes in and out of frame
      if (active) {
        const x = 70 + p * 110 + Math.sin((f + p * 10) * 0.1) * 8;
        const y = 140 + Math.cos((f + p * 5) * 0.1) * 6;

        // Head oval with skin tone
        ctx.fillStyle = '#fed7aa'; // skin tone for detection
        ctx.beginPath();
        ctx.ellipse(x + 35, y + 40, 30, 38, 0, 0, Math.PI * 2);
        ctx.fill();

        // Hair
        ctx.fillStyle = colors[p % colors.length];
        ctx.beginPath();
        ctx.arc(x + 35, y + 25, 32, Math.PI, 0);
        ctx.fill();

        // Eyes & Smile
        ctx.fillStyle = '#0f172a';
        ctx.beginPath();
        ctx.arc(x + 25, y + 36, 3, 0, Math.PI * 2);
        ctx.arc(x + 45, y + 36, 3, 0, Math.PI * 2);
        ctx.fill();

        ctx.beginPath();
        ctx.arc(x + 35, y + 50, 10, 0, Math.PI);
        ctx.stroke();

        // Name badge
        ctx.fillStyle = '#ffffff';
        ctx.font = '12px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(names[p % names.length], x + 35, y + 95);
      }
    }
    await new Promise((r) => setTimeout(r, 25));
  }

  mediaRecorder.stop();
  mediaRecorder.onstop = () => {
    const blob = new Blob(chunks, { type: 'video/webm' });
    const file = new File([blob], `${title.replace(/\s+/g, '_')}.webm`, { type: 'video/webm' });
    handleVideoSelected(file);
  };
}

// Full Pipeline Runner
btnProcessVideo.addEventListener('click', async () => {
  if (!currentVideoInfo) return;

  welcomePlaceholder.classList.add('hidden');
  resultStudio.classList.add('hidden');
  pipelineCard.classList.remove('hidden');
  btnProcessVideo.setAttribute('disabled', 'true');

  faceTracker.reset();
  liveFacesFeed.innerHTML = '';

  const fps = parseFloat(fpsSlider.value);
  const threshold = parseFloat(threshSlider.value);
  const timestamps = videoExtractor.calculateSampleTimes(currentVideoInfo.duration, fps);

  let totalDetections = 0;

  // 1. Extraction & Detection Loop
  setPipelineStep('step-extract', 'Extracting Frames & Detecting Faces...');
  statFrames.textContent = `Frames: 0/${timestamps.length}`;

  for (let i = 0; i < timestamps.length; i++) {
    const ts = timestamps[i];
    await videoExtractor.seekFrame(ts);
    const { canvas: frameCanvas } = videoExtractor.captureFrame();

    // Draw live frame
    liveCanvas.width = 480;
    liveCanvas.height = (480 * frameCanvas.height) / frameCanvas.width;
    liveCtx.drawImage(frameCanvas, 0, 0, liveCanvas.width, liveCanvas.height);

    // Detect faces
    const detections = faceDetector.detectFaces(frameCanvas, ts, i);

    if (detections.length > 0) {
      totalDetections += detections.length;
      faceTracker.processFrame(detections, ts);

      // Draw bounding boxes on live canvas
      const scaleX = liveCanvas.width / frameCanvas.width;
      const scaleY = liveCanvas.height / frameCanvas.height;

      liveCtx.strokeStyle = '#6366f1';
      liveCtx.lineWidth = 3;
      for (const d of detections) {
        liveCtx.strokeRect(
          d.boundingBox.x * scaleX,
          d.boundingBox.y * scaleY,
          d.boundingBox.width * scaleX,
          d.boundingBox.height * scaleY
        );

        // Add thumbnail to feed
        if (liveFacesFeed.children.length < 24) {
          const thumbCard = document.createElement('div');
          thumbCard.className = 'face-thumb-card';
          const img = document.createElement('img');
          img.src = d.cropDataUrl;
          thumbCard.appendChild(img);
          liveFacesFeed.prepend(thumbCard);
        }
      }
    }

    const pct = Math.round(((i + 1) / timestamps.length) * 55);
    progressBar.style.width = `${pct}%`;
    statFrames.textContent = `Frames: ${i + 1}/${timestamps.length}`;
    statFaces.textContent = `Faces: ${totalDetections}`;
    statTracks.textContent = `Tracks: ${faceTracker.getFinalTracks().length}`;

    await new Promise((r) => setTimeout(r, 10));
  }

  // 2. Tracking & Embeddings
  setPipelineStep('step-track', 'Spatial-Temporal Face Tracking');
  setPipelineStep('step-embed', 'Computing Feature Embeddings');
  progressBar.style.width = '70%';

  const allTracks = faceTracker.getFinalTracks();

  for (const track of allTracks) {
    const embeddings = track.detections.map((d) => faceEmbedder.extractEmbedding(d.cropCanvas));
    track.averageEmbedding = identityClusterer.computeCentroid(embeddings);
  }

  // 3. Identity Clustering
  setPipelineStep('step-cluster', 'Clustering Unique Human Identities');
  progressBar.style.width = '85%';
  const clusters = identityClusterer.clusterTracks(allTracks, threshold);

  // 4. Best Shot Quality Scoring
  setPipelineStep('step-score', 'Selecting Highest Quality Portraits');
  progressBar.style.width = '92%';

  const persons = clusters.map((cluster, idx) => {
    const { bestDetection, qualityScore } = qualityScorer.selectBestShot(cluster.tracks);
    const appearanceCount = cluster.tracks.length;
    const durationSec = cluster.tracks.reduce((acc, t) => acc + (t.lastTimestamp - t.startTime + 0.5), 0);

    return {
      id: `person_${idx + 1}`,
      name: `Person #${idx + 1}`,
      bestDetection,
      qualityScore,
      appearanceCount,
      screenTime: `${Math.round(durationSec)}s`,
      tracks: cluster.tracks
    };
  });

  // Sort by appearance frequency
  persons.sort((a, b) => b.appearanceCount - a.appearanceCount);
  persons.forEach((p, idx) => (p.name = `Person #${idx + 1}`));
  currentPersonsResult = persons;

  // 5. High-Resolution Collage Rendering
  setPipelineStep('step-collage', 'Rendering High-Resolution Collage');
  progressBar.style.width = '100%';

  await new Promise((r) => setTimeout(r, 400));
  collageRenderer.renderCollage(collageCanvas, persons, selectedStyle);

  // Show results
  pipelineCard.classList.add('hidden');
  resultStudio.classList.remove('hidden');
  btnProcessVideo.removeAttribute('disabled');

  resultSummaryText.textContent = `Identified ${persons.length} unique individuals across ${timestamps.length} video frames`;
  renderPersonsList(persons);

  // Celebration
  confetti({
    particleCount: 80,
    spread: 70,
    origin: { y: 0.6 }
  });
});

function setPipelineStep(stepId, title) {
  document.querySelectorAll('.step-item').forEach((el) => el.classList.remove('active'));
  const current = document.getElementById(stepId);
  if (current) current.classList.add('active');
  stageTitle.textContent = title;
}

function renderPersonsList(persons) {
  personsList.innerHTML = '';
  persons.forEach((p) => {
    const card = document.createElement('div');
    card.className = 'person-item-card';

    const img = document.createElement('img');
    img.className = 'person-avatar';
    img.src = p.bestDetection?.cropDataUrl || '';

    const info = document.createElement('div');
    info.className = 'person-info';

    const name = document.createElement('span');
    name.className = 'person-name';
    name.textContent = p.name;

    const stats = document.createElement('span');
    stats.className = 'person-stats';
    stats.textContent = `${p.appearanceCount} appearances • ${p.screenTime} screen time`;

    const score = document.createElement('span');
    score.className = 'person-score';
    score.textContent = `★ Quality: ${(p.qualityScore * 100).toFixed(0)}%`;

    info.appendChild(name);
    info.appendChild(stats);
    info.appendChild(score);

    card.appendChild(img);
    card.appendChild(info);
    personsList.appendChild(card);
  });
}

// Download Collage Image
btnDownloadCollage.addEventListener('click', () => {
  const link = document.createElement('a');
  link.download = `UniquePersonCollage_${Date.now()}.png`;
  link.href = collageCanvas.toDataURL('image/png');
  link.click();
});

// Web Share API
btnShareCollage.addEventListener('click', async () => {
  if (navigator.share && collageCanvas.toBlob) {
    collageCanvas.toBlob(async (blob) => {
      const file = new File([blob], 'UniquePersonCollage.png', { type: 'image/png' });
      try {
        await navigator.share({
          title: 'Unique Person Collage',
          text: 'Check out the unique people detected in this video!',
          files: [file]
        });
      } catch (err) {
        // Fallback to download
        btnDownloadCollage.click();
      }
    });
  } else {
    btnDownloadCollage.click();
  }
});
