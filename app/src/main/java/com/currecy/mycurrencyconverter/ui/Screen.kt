package com.currecy.mycurrencyconverter.ui

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class Screen(
    private val cutoutRadius: Float // This is the FAB radius plus any desired margin
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = 0f

        // Define the rectangle that will be subtracted to create the notch
        val cutoutRect = Rect(
            left = centerX - cutoutRadius,
            top = -cutoutRadius,
            right = centerX + cutoutRadius,
            bottom = cutoutRadius
        )

        path.moveTo(0f, 0f)
        path.lineTo(cutoutRect.left, 0f)
        // Draw the notch (arc)
        path.arcTo(
            rect = cutoutRect,
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )
        path.lineTo(size.width, 0f)
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()

        return Outline.Generic(path)
    }
}




