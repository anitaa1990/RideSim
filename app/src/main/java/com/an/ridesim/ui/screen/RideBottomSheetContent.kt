package com.an.ridesim.ui.screen

import androidx.compose.runtime.Composable

/**
 * [RideBottomSheetContent] serves as the wrapper for all the ride related content that
 * should be displayed inside the bottom sheet.
 */
@Composable
fun RideBottomSheetContent(
    pickupText: String,
    dropText: String,
    onPickupChange: (String) -> Unit,
    onDropChange: (String) -> Unit
) {
    RideInputSection(
        pickupText = pickupText,
        dropText = dropText,
        onPickupChange = onPickupChange,
        onDropChange = onDropChange
    )
}
