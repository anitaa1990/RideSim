package com.an.ridesim.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun DashedDivider(
    width: Float = 0.55f
) {
    val color = MaterialTheme.colorScheme.onTertiaryContainer
    Canvas(
        modifier = Modifier
            .fillMaxWidth(width)
            .height(1.dp)
    ) {
        val dashWidth = 10f
        val dashGap = 6f
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x + dashWidth, 0f),
                strokeWidth = size.height
            )
            x += dashWidth + dashGap
        }
    }
}