package com.vignesh.uniquepersoncollage.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.vignesh.uniquepersoncollage.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * Saves generated collages and portraits to the device MediaStore gallery.
 */
class GallerySaver(private val context: Context) {

    /**
     * Saves a Bitmap to Pictures/UniquePersonCollage directory.
     */
    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        title: String = "UniquePersons_${System.currentTimeMillis()}"
    ): Uri? = withContext(Dispatchers.IO) {
        val filename = "$title.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UniquePersonCollage")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                Logger.i("Saved collage to gallery successfully at URI: $uri")
                return@withContext uri
            } catch (e: Exception) {
                Logger.e("Failed to write image data to MediaStore", e)
                resolver.delete(uri, null, null)
            }
        }
        null
    }
}
