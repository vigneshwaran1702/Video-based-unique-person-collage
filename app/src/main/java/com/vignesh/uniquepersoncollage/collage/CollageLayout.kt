package com.vignesh.uniquepersoncollage.collage

import android.graphics.RectF
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Supported collage styles and layout coordinate generators.
 */
enum class CollageStyle(val title: String, val description: String) {
    DYNAMIC_GRID("Modern Grid", "Adaptive rectangular grid layout with subtle shadows"),
    POLAROID("Polaroid Gallery", "White bordered instant-film photo cards with titles"),
    HONEYCOMB("Circular Mosaic", "Clean circular badge portraits arranged symmetrically"),
    HERO_GRID("Leaderboard / Hero", "Top frequent person highlighted as Hero with grid below")
}

class CollageLayout {

    /**
     * Computes the bounding rectangles for each person tile within canvas dimensions.
     */
    fun calculateTileBounds(
        style: CollageStyle,
        count: Int,
        canvasWidth: Int,
        canvasHeight: Int,
        paddingPx: Float = 32f
    ): List<RectF> {
        if (count <= 0) return emptyList()

        return when (style) {
            CollageStyle.DYNAMIC_GRID -> calculateDynamicGrid(count, canvasWidth, canvasHeight, paddingPx)
            CollageStyle.POLAROID -> calculatePolaroidGrid(count, canvasWidth, canvasHeight, paddingPx)
            CollageStyle.HONEYCOMB -> calculateHoneycomb(count, canvasWidth, canvasHeight, paddingPx)
            CollageStyle.HERO_GRID -> calculateHeroGrid(count, canvasWidth, canvasHeight, paddingPx)
        }
    }

    private fun calculateDynamicGrid(
        count: Int,
        canvasWidth: Int,
        canvasHeight: Int,
        padding: Float
    ): List<RectF> {
        val cols = ceil(sqrt(count.toDouble())).toInt()
        val rows = ceil(count.toDouble() / cols).toInt()

        val availableWidth = canvasWidth - (padding * (cols + 1))
        val availableHeight = canvasHeight - (padding * (rows + 1))

        val tileWidth = availableWidth / cols
        val tileHeight = availableHeight / rows

        val bounds = mutableListOf<RectF>()

        for (i in 0 until count) {
            val r = i / cols
            val c = i % cols

            val left = padding + c * (tileWidth + padding)
            val top = padding + r * (tileHeight + padding)
            bounds.add(RectF(left, top, left + tileWidth, top + tileHeight))
        }

        return bounds
    }

    private fun calculatePolaroidGrid(
        count: Int,
        canvasWidth: Int,
        canvasHeight: Int,
        padding: Float
    ): List<RectF> {
        // Similar to grid with extra bottom margin for caption card
        return calculateDynamicGrid(count, canvasWidth, canvasHeight, padding * 1.5f)
    }

    private fun calculateHoneycomb(
        count: Int,
        canvasWidth: Int,
        canvasHeight: Int,
        padding: Float
    ): List<RectF> {
        val gridBounds = calculateDynamicGrid(count, canvasWidth, canvasHeight, padding)
        return gridBounds.map { rect ->
            val size = minOf(rect.width(), rect.height())
            val cx = rect.centerX()
            val cy = rect.centerY()
            RectF(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
        }
    }

    private fun calculateHeroGrid(
        count: Int,
        canvasWidth: Int,
        canvasHeight: Int,
        padding: Float
    ): List<RectF> {
        if (count == 1) return calculateDynamicGrid(1, canvasWidth, canvasHeight, padding)

        val bounds = mutableListOf<RectF>()

        // Hero on Left (half width) or Top
        val heroWidth = (canvasWidth - (padding * 3)) * 0.45f
        val heroHeight = canvasHeight - (padding * 2)
        bounds.add(RectF(padding, padding, padding + heroWidth, padding + heroHeight))

        val remainingCount = count - 1
        val gridLeft = padding * 2 + heroWidth
        val gridWidth = canvasWidth - gridLeft - padding
        val gridHeight = canvasHeight - (padding * 2)

        val subGrid = calculateDynamicGrid(remainingCount, gridWidth.toInt(), gridHeight.toInt(), padding)
        for (rect in subGrid) {
            bounds.add(RectF(rect.left + gridLeft, rect.top + padding, rect.right + gridLeft, rect.bottom + padding))
        }

        return bounds
    }
}
