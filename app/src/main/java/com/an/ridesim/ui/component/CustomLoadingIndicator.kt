package com.an.ridesim.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Composable
fun CustomLoadingIndicator(
    size: Int = 8,  // Size of the indicator
    color: Color = Color.White,  // Color of the dots,
    modifier: Modifier = Modifier
) {
    // Infinite transition for animations
    val transition = rememberInfiniteTransition(label = "")

    // Dot 1 animation (Starts immediately)
    val dot1Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Dot 2 animation (Starts with a delay)
    val dot2Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Dot 3 animation (Starts with a larger delay)
    val dot3Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Shared modifier for all three dots
    fun Modifier.dotBehaviour(anchor: Float) = this
        .offset {
            // Vertical bounce animation
            IntOffset(0, (-2 * size * anchor.dp.toPx()).toInt())
        }
        .scale(lerp(1f, 1.25f, anchor)) // Size pulse effect
//        .alpha(lerp(0.7f, 1f, anchor)) // Fade-in/out effect

    // Composable structure for the dots
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(4.dp)
    ) {
        // First Dot
        Box(
            modifier = Modifier
                .dotBehaviour(dot1Offset)
                .size(size.dp)
                .clip(CircleShape)
                .background(color)
        )

        // Second Dot
        Box(
            modifier = Modifier
                .dotBehaviour(dot2Offset)
                .size(size.dp)
                .clip(CircleShape)
                .background(color)
        )

        // Third Dot
        Box(
            modifier = Modifier
                .dotBehaviour(dot3Offset)
                .size(size.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingIndicatorPreview() {
    CustomLoadingIndicator(size = 8, color = Color.Black)
}
