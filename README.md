# UniquePersonCollage 📸✨

An intelligent Android application that processes input videos, detects and tracks human faces, extracts deep feature embeddings, clusters unique human identities, selects the best representative portraits, and automatically generates high-resolution artistic collages.

---

## 🧠 Architecture & Pipeline

```
                    ┌──────────────┐
                    │ Select Video │
                    └──────┬───────┘
                           ↓
                  ┌─────────────────┐
                  │ Frame Extraction│
                  └────────┬────────┘
                           ↓
                  ┌─────────────────┐
                  │ Face Detection  │
                  │    ML Kit       │
                  └────────┬────────┘
                           ↓
                  ┌─────────────────┐
                  │ Face Tracking   │
                  └────────┬────────┘
                           ↓
                  ┌─────────────────┐
                  │ Face Embedding  │
                  │     TFLite      │
                  └────────┬────────┘
                           ↓
                  ┌─────────────────┐
                  │ Identity        │
                  │ Clustering      │
                  └────────┬────────┘
                           ↓
                  ┌─────────────────┐
                  │ Appearance      │
                  │ Counting        │
                  └────────┬────────┘
                           ↓
                  ┌─────────────────┐
                  │ Best Shot       │
                  │ Selection       │
                  └────────┬────────┘
                           ↓
                  ┌─────────────────┐
                  │ Collage         │
                  │ Generation      │
                  └────────┬────────┘
                           ↓
              ┌────────────┴────────────┐
              ↓                         ↓
        ┌──────────┐              ┌──────────┐
        │  Save    │              │  Share   │
        │ Gallery  │              │  Sheet   │
        └──────────┘              └──────────┘
```

---

## 🚀 Key Features

1. **Smart Frame Extraction**: Adaptive sampling with `MediaMetadataRetriever` optimized for long & short video clips.
2. **On-Device ML Kit Face Detection**: High-accuracy face landmark and head pose Euler angle extraction.
3. **Spatial-Temporal Tracking**: Real-time IoU + distance Hungarian tracking across frames.
4. **Deep Feature Embeddings**: TFLite MobileFaceNet extracting normalized facial vector representations.
5. **Agglomerative Identity Clustering**: Dynamic distance thresholding grouping repeated appearances into unique persons.
6. **Multi-Factor Quality Scoring**: Laplacian variance sharpness, frontal angle weight, and size scoring to pick each person's crispest portrait.
7. **Artistic Collage Engine**: High-res canvas renderer supporting Dynamic Grid, Honeycomb, Masonry, Polaroid, and Leaderboard layouts with shadows, badges, and gradients.
8. **Modern Jetpack Compose UI**: Glassmorphic dark/light UI, animated multi-stage progress, and interactive person detail screens.
9. **Export & Share**: Direct export to Android MediaStore gallery and system Share Sheet.

---

## 🛠️ Tech Stack & Requirements

- **Language**: Kotlin 2.0+
- **UI Toolkit**: Jetpack Compose + Material 3
- **ML & Vision**: Google ML Kit Face Detection, TensorFlow Lite
- **Asynchronous**: Kotlin Coroutines & Flows
- **Image Loading**: Coil 2.7
- **Min SDK**: API 26 (Android 8.0)
- **Target SDK**: API 35 (Android 15)

