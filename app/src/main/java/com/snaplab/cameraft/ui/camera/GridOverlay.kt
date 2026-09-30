package com.snaplab.cameraft.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 1f
        val gridColor = Color(0x55FFFFFF)

        // Vertical lines
        val x1 = size.width / 3f
        val x2 = size.width * 2f / 3f
        drawLine(gridColor, Offset(x1, 0f), Offset(x1, size.height), strokeWidth)
        drawLine(gridColor, Offset(x2, 0f), Offset(x2, size.height), strokeWidth)

        // Horizontal lines
        val y1 = size.height / 3f
        val y2 = size.height * 2f / 3f
        drawLine(gridColor, Offset(0f, y1), Offset(size.width, y1), strokeWidth)
        drawLine(gridColor, Offset(0f, y2), Offset(size.width, y2), strokeWidth)
    }
}
