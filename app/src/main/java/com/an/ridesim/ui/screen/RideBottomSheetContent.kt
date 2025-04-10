package com.an.ridesim.ui.screen

import androidx.compose.runtime.Composable
import com.an.ridesim.model.TripState
import com.an.ridesim.model.VehicleDetail
import com.an.ridesim.model.hasRideStarted
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
    onSuggestionSelected: (AutocompletePrediction) -> Unit,
    onVehicleSelected: (VehicleDetail) -> Unit
) {
    // Show the input section if pickup or drop is not selected
    if (uiState.pickupLocation == null || uiState.dropLocation == null || uiState.routePolyline.isEmpty()) {
        // Show address input section when locations are not selected yet
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
    } else if (uiState.tripState == TripState.IDLE) {
        RideDetailSection(
            uiState = uiState,
            onVehicleSelected = { onVehicleSelected(it) }
        )
    } else if (uiState.tripState.hasRideStarted()) {
        RideStartedSection(
            rideUiModel = uiState.rideUiModel,
            pickupLocation = uiState.pickupLocation,
            dropLocation = uiState.dropLocation,
            vehicleDetail = uiState.selectedVehicle
        )
    }
}
