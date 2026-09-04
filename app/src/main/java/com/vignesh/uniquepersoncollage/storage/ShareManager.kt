package com.vignesh.uniquepersoncollage.storage

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.vignesh.uniquepersoncollage.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Handles sharing images with other Android applications via FileProvider.
 */
class ShareManager(private val context: Context) {

    /**
     * Creates a share intent for the provided Bitmap.
     */
    suspend fun shareBitmap(
        bitmap: Bitmap,
        title: String = "Unique Person Collage",
        caption: String = "Unique persons detected in my video with #UniquePersonCollage"
    ): Intent? = withContext(Dispatchers.IO) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_collage_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Intent.createChooser(shareIntent, "Share Unique Person Collage")
        } catch (e: Exception) {
            Logger.e("Failed to prepare share intent", e)
            null
        }
    }
}
