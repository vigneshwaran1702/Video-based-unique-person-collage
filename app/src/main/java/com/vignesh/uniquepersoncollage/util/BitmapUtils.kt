package com.vignesh.uniquepersoncollage.util

import android.graphics.*
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

object BitmapUtils {

    /**
     * Crops a region with optional margin expansion and bounds checking.
     */
    fun cropFaceWithMargin(
        source: Bitmap,
        rect: Rect,
        marginPercent: Float = 0.25f
    ): Bitmap? {
        val width = source.width
        val height = source.height

        val marginX = (rect.width() * marginPercent).toInt()
        val marginY = (rect.height() * marginPercent).toInt()

        val left = max(0, rect.left - marginX)
        val top = max(0, rect.top - marginY)
        val right = min(width, rect.right + marginX)
        val bottom = min(height, rect.bottom + marginY)

        val cropWidth = right - left
        val cropHeight = bottom - top

        if (cropWidth <= 0 || cropHeight <= 0) return null

        return try {
            Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a circular masked bitmap from the given source.
     */
    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val srcRect = Rect(
            (bitmap.width - size) / 2,
            (bitmap.height - size) / 2,
            (bitmap.width + size) / 2,
            (bitmap.height + size) / 2
        )
        canvas.drawBitmap(bitmap, srcRect, rect, paint)

        return output
    }

    /**
     * Creates a rounded corner rectangle bitmap.
     */
    fun getRoundedCornerBitmap(bitmap: Bitmap, cornerRadiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    /**
     * Resizes a bitmap preserving aspect ratio with a max dimension.
     */
    fun resizeWithAspect(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap

        val scale = if (w > h) maxDim.toFloat() / w else maxDim.toFloat() / h
        val newW = (w * scale).toInt()
        val newH = (h * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    /**
     * Converts a bitmap into a Grayscale byte array for fast computer vision processing.
     */
    fun toGrayscaleByteArray(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val gray = ByteArray(width * height)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            // Luminance formula
            gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt().toByte()
        }
        return gray
    }
}
