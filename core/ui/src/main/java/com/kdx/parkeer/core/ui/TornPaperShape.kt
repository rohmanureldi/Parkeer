package com.kdx.parkeer.core.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TornPaperShape(private val teethHeight: Dp = 8.dp, private val teethWidth: Dp = 12.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val th = with(density) { teethHeight.toPx() }
        val tw = with(density) { teethWidth.toPx() }
        val path = Path().apply {
            // Top zigzag
            moveTo(0f, th)
            var x = 0f
            while (x + tw <= size.width) {
                lineTo(x + tw / 2, 0f)
                lineTo(x + tw, th)
                x += tw
            }
            if (x < size.width) {
                lineTo((x + size.width) / 2, 0f)
                lineTo(size.width, th)
            }
            // Right edge
            lineTo(size.width, size.height - th)
            // Bottom zigzag (right to left)
            x = size.width
            while (x - tw >= 0f) {
                lineTo(x - tw / 2, size.height)
                lineTo(x - tw, size.height - th)
                x -= tw
            }
            if (x > 0f) {
                lineTo(x / 2, size.height)
                lineTo(0f, size.height - th)
            }
            close()
        }
        return Outline.Generic(path)
    }
}
