package com.eldirohmanur.parkeer.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

@Composable
fun ArcBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val peakY = size.height * 0.30f
            val arcHeight = size.width * 0.25f
            val shadowOffset = 40f

            // Shadow: a gradient fade above the arc edge
            val shadowPath = Path().apply {
                moveTo(0f, peakY + shadowOffset)
                quadraticTo(
                    size.width / 2f,
                    peakY - arcHeight + shadowOffset,
                    size.width,
                    peakY + shadowOffset,
                )
                lineTo(size.width, peakY - arcHeight)
                lineTo(0f, peakY - arcHeight)
                close()
            }
            drawPath(
                shadowPath,
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF0E2FB3).copy(alpha = 0.3f)),
                    startY = peakY - arcHeight,
                    endY = peakY + shadowOffset,
                ),
            )

            // White arc fill
            val path = Path().apply {
                moveTo(0f, peakY)
                quadraticTo(size.width / 2f, peakY - arcHeight, size.width, peakY)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, Color.White)
        }
        content()
    }
}
