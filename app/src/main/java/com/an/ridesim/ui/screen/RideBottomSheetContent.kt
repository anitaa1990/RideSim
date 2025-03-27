package com.an.ridesim.ui.screen

import androidx.compose.runtime.Composable
import com.an.ridesim.ui.viewmodel.AddressFieldType
import com.an.ridesim.ui.viewmodel.RideViewModel.RideUiState
import com.google.android.libraries.places.api.model.AutocompletePrediction

/**
 * [RideBottomSheetContent] serves as the dynamic wrapper for the bottom sheet content.
 *
 * It renders different UI states based on:
 * - Whether pickup/drop is set
 * - Whether trip has started
 * - Whether prediction list should be shown
 */
@Composable
fun RideBottomSheetContent(
    uiState: RideUiState,
    pickupInput: String,
    dropInput: String,
    onPickupChange: (String) -> Unit,
    onDropChange: (String) -> Unit,
    onFieldFocusChanged: (AddressFieldType) -> Unit,
    onSuggestionSelected: (AutocompletePrediction) -> Unit
) {
    RideInputSection(
        pickupText = pickupInput,
        dropText = dropInput,
        onPickupChange = onPickupChange,
        onDropChange = onDropChange,
        onFieldFocusChanged = onFieldFocusChanged,
        suggestions = when (uiState.focusedField) {
            AddressFieldType.PICKUP -> uiState.pickupSuggestions
            AddressFieldType.DROP -> uiState.dropSuggestions
            else -> emptyList()
        },
        onSuggestionSelected = onSuggestionSelected
    )
}
