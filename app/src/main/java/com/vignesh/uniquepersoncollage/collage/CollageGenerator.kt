package com.vignesh.uniquepersoncollage.collage

import android.content.Context
import android.graphics.*
import com.vignesh.uniquepersoncollage.data.model.Person
import com.vignesh.uniquepersoncollage.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-resolution canvas renderer creating artistic collages of unique persons.
 */
class CollageGenerator(private val context: Context) {

    private val collageLayout = CollageLayout()
    private val imageCropper = ImageCropper()

    /**
     * Generates a high-resolution collage bitmap for the given list of unique persons.
     */
    suspend fun generateCollage(
        persons: List<Person>,
        style: CollageStyle = CollageStyle.DYNAMIC_GRID,
        width: Int = Constants.COLLAGE_DEFAULT_WIDTH,
        height: Int = Constants.COLLAGE_DEFAULT_HEIGHT
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw modern gradient background
        drawBackground(canvas, width, height)

        if (persons.isEmpty()) {
            drawEmptyState(canvas, width, height)
            return@withContext bitmap
        }

        // 2. Calculate layout geometry
        val bounds = collageLayout.calculateTileBounds(style, persons.size, width, height)

        // 3. Render each person tile
        for (i in persons.indices) {
            val person = persons[i]
            val rect = bounds[i]
            drawPersonTile(canvas, person, rect, style, i)
        }

        // 4. Draw Header / Watermark Title
        drawHeaderTitle(canvas, persons.size, width)

        bitmap
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int) {
        val gradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(
                Color.parseColor("#0F172A"), // Slate 900
                Color.parseColor("#1E1B4B"), // Indigo 950
                Color.parseColor("#090D16")
            ),
            null,
            Shader.TileMode.CLAMP
        )
        val paint = Paint().apply {
            shader = gradient
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawPersonTile(
        canvas: Canvas,
        person: Person,
        rect: RectF,
        style: CollageStyle,
        index: Int
    ) {
        val face = person.representativeFace ?: return
        val isCircular = (style == CollageStyle.HONEYCOMB)

        // Shadow / Glow card effect
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33000000")
            maskFilter = BlurMaskFilter(16f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(rect, 24f, 24f, shadowPaint)

        // Card Background
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
        }
        canvas.drawRoundRect(rect, 24f, 24f, cardPaint)

        // Prepare face bitmap
        val imageRect = if (style == CollageStyle.POLAROID) {
            RectF(rect.left + 16f, rect.top + 16f, rect.right - 16f, rect.bottom - 70f)
        } else {
            RectF(rect.left + 12f, rect.top + 12f, rect.right - 12f, rect.bottom - 60f)
        }

        if (imageRect.width() > 10 && imageRect.height() > 10) {
            val tileBitmap = imageCropper.prepareCollageTile(
                face,
                imageRect.width().toInt(),
                imageRect.height().toInt(),
                isCircular = isCircular,
                cornerRadiusPx = if (isCircular) 0f else 18f
            )
            canvas.drawBitmap(tileBitmap, imageRect.left, imageRect.top, null)
        }

        // Person Badge / Name
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = (rect.height() * 0.07f).coerceIn(24f, 40f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = (rect.height() * 0.05f).coerceIn(18f, 28f)
            textAlign = Paint.Align.CENTER
        }

        val textY = rect.bottom - 36f
        val subTextY = rect.bottom - 12f
        val textX = rect.centerX()

        canvas.drawText(person.name, textX, textY, textPaint)
        canvas.drawText("${person.appearanceCount} appearances • ${person.screenTimeFormatted}", textX, subTextY, subTextPaint)
    }

    private fun drawHeaderTitle(canvas: Canvas, personCount: Int, width: Int) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6366F1")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("Unique Persons ($personCount)", 40f, 45f, titlePaint)
    }

    private fun drawEmptyState(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 48f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("No unique faces detected in video", width / 2.0f, height / 2.0f, paint)
    }
}
