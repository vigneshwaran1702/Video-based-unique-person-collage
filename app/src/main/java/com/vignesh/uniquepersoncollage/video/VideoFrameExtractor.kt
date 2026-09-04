package com.vignesh.uniquepersoncollage.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.vignesh.uniquepersoncollage.data.model.VideoInfo
import com.vignesh.uniquepersoncollage.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts frames and metadata from input video files using MediaMetadataRetriever.
 */
class VideoFrameExtractor(private val context: Context) {

    /**
     * Reads metadata of the target video.
     */
    suspend fun extractVideoInfo(uri: Uri): VideoInfo? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "video/mp4"

            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val width = widthStr?.toIntOrNull() ?: 1280
            val height = heightStr?.toIntOrNull() ?: 720
            val rotation = rotationStr?.toIntOrNull() ?: 0

            var filename = "Video_${System.currentTimeMillis()}"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        filename = cursor.getString(nameIndex)
                    }
                }
            }

            VideoInfo(
                uri = uri,
                name = filename,
                durationMs = durationMs,
                width = if (rotation == 90 || rotation == 270) height else width,
                height = if (rotation == 90 || rotation == 270) width else height,
                rotation = rotation,
                fps = 30.0f,
                mimeType = mimeType
            )
        } catch (e: Exception) {
            Logger.e("Failed to extract video metadata for URI: $uri", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Logger.w("Retriever release error", e)
            }
        }
    }

    /**
     * Extracts a single frame at a specific timestamp in milliseconds.
     */
    suspend fun getFrameAtTime(uri: Uri, timeMs: Long): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val timeUs = timeMs * 1000L
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            Logger.e("Error extracting frame at ${timeMs}ms", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
