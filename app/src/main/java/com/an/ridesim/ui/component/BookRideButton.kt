package com.an.ridesim.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.an.ridesim.R

@Composable
fun BookRideButton(
    price: Double,
    onBookRide: () -> Unit
) {
    val thumbWidthDp = 70.dp
    val density = LocalDensity.current
    val thumbWidthPx = with(density) { thumbWidthDp.toPx() }

    var sliderPositionPx by remember { mutableFloatStateOf(0f) } // Slider Position in Px
    var boxWidthPx by remember { mutableIntStateOf(0) } // Width of the entire Box
    var sliderComplete by remember { mutableStateOf(false) } // To detect when slider reaches the right end
    var showLoading by remember { mutableStateOf(false) } // To show the loading indicator

    // Calculate drag progress (0f to 1f)
    val dragProgress = remember(sliderPositionPx, boxWidthPx) {
        if (boxWidthPx > 0) {
            (sliderPositionPx / (boxWidthPx - thumbWidthPx)).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    // Text Transition
    val textAlpha = 1f - dragProgress

    // Animate the scale of the button when slider reaches the right end
    val trackScale by animateFloatAsState(
        targetValue = if (sliderComplete) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300), label = "",
    )

    // Animate thumb disappearance
    val thumbAlpha by animateFloatAsState(
        targetValue = if (sliderComplete) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300), label = "",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp) // Adjust the height of the button
            .onSizeChanged { size ->
                boxWidthPx = size.width
            }
    ) {
        // Scale the track only, not the thumb
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(scaleX = trackScale, scaleY = 1f) // Scale only the track
                .background(Color.Black, shape = RoundedCornerShape(10.dp))
        ) {
            // Centered text on the track
            Text(
                text = String.format(stringResource(R.string.btn_book_ride), price),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFFAC901)
                ),
                modifier = Modifier.align(Alignment.Center)
                    .alpha(textAlpha), // Apply alpha based on drag progress
            )
        }

        // Slider Thumb - Move along the track and disappear after completion
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(1.dp)
                .offset(x = (sliderPositionPx / density.density).dp)
                .draggable(
                    state = rememberDraggableState { delta ->
                        val newPos = sliderPositionPx + delta
                        if (newPos >= 0 && newPos <= (boxWidthPx - thumbWidthPx)) {
                            sliderPositionPx = newPos
                        }
                    },
                    orientation = Orientation.Horizontal,
                    onDragStarted = {},
                    onDragStopped = {
                        // Detect when the slider reaches the right end
                        if (dragProgress >= 0.99f) {
                            sliderComplete = true
                            onBookRide()
                            // Start showing the loading spinner after slider reaches the end
                            showLoading = true
                        }
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .alpha(thumbAlpha) // Apply alpha to make thumb disappear
                    .graphicsLayer {
                        alpha = thumbAlpha  // Ensure both the background and the content fade out
                    },
            ) {
                // Slider button (thumb) - make it disappear at the end
                SliderButton() // Pass thumbAlpha to make it disappear
            }
        }

        // Show the loading indicator after the slider reaches the end and the animation completes
        if (showLoading) {
            CircularProgressIndicator(
                color = Color.Black,
                strokeWidth = 3.dp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun SliderButton() {
    Box(
        modifier = Modifier
            .wrapContentSize()
            .width(70.dp)
            .height(54.dp)
            .background(Color(0xFFFAC901), shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_car),
                contentDescription = "Car Icon",
                modifier = Modifier.size(25.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            Image(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = "Arrow Icon",
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookRideButtonPreview() {
    BookRideButton(
        price = 191.0, // Example price
        onBookRide = { /* Handle slide event */ }
    )
}

@Preview(showBackground = true)
@Composable
fun SliderButtonPreview() {
    SliderButton()
}

