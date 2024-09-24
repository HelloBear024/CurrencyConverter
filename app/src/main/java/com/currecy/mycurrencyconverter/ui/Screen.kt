package com.currecy.mycurrencyconverter.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class Screen(
    private val cutoutRadius: Float,
    private val cornerRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f

        val cutoutRect = androidx.compose.ui.geometry.Rect(
            left = centerX - cutoutRadius,
            top = -cutoutRadius,
            right = centerX + cutoutRadius,
            bottom = cutoutRadius
        )

        path.moveTo(0f, cornerRadius)

        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = 0f,
                top = 0f,
                right = cornerRadius * 2,
                bottom = cornerRadius * 2
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        path.lineTo(cutoutRect.left, 0f)

        path.arcTo(
            rect = cutoutRect,
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )


        path.lineTo(size.width - cornerRadius, 0f)

        path.arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = size.width - cornerRadius * 2,
                top = 0f,
                right = size.width,
                bottom = cornerRadius * 2
            ),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        path.lineTo(size.width, size.height)

        path.lineTo(0f, size.height)

        path.close()

        return Outline.Generic(path)
    }
}




