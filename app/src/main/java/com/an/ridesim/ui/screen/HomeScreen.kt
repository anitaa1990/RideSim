package com.an.ridesim.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.ridesim.model.LatLngPoint
import com.an.ridesim.model.TripState
import com.an.ridesim.model.peekHeight
import com.an.ridesim.model.shouldFollowCar
import com.an.ridesim.model.toLatLng
import com.an.ridesim.ui.component.GoogleMapView
import com.an.ridesim.ui.viewmodel.AddressFieldType
import com.an.ridesim.ui.viewmodel.RideViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * [HomeScreen] is the main Composable for the RideSim ride-booking experience.
 *
 * Users can:
 * - View a live map centered around their current location
 * - Enter pickup and drop addresses
 * - See address predictions via Google Places API
 * - View a route (polyline) between pickup and drop
 * - Simulate a trip (with animated car icon)
 *
 * Architecture:
 * - Uses [RideViewModel] for all state and side-effects
 * - Integrates Google Maps Compose for map rendering
 * - Displays ride inputs in a bottom sheet (using BottomSheetScaffold)
 *
 * Behavior:
 * - Animates camera on pickup OR when both pickup & drop are selected
 * - Expands bottom sheet when input field is focused
 * - Keeps map interactive even when trip is active
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RideViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(LocalLifecycleOwner.current)
    val cameraPositionState = rememberCameraPositionState()

    val focusManager = LocalFocusManager.current
    val sheetState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // Ask for location permission (or show a request screen)
    if (!uiState.isPermissionGranted) {
        RequestPermissionScreen(onPermissionChanged = viewModel::updatePermissionState)
    } else {
        viewModel.fetchCurrentLocationAsPickup()
    }

    // Animate camera to show both pickup & drop when route is available
    LaunchedEffect(
        uiState.tripState, // Watch for trip state changes
        uiState.pickupLocation, // Watch for pickup location changes
        uiState.dropLocation, // Watch for drop location changes
        uiState.carPosition // Watch for the car's position changes
    ) {
        when (uiState.tripState) {
            TripState.IDLE -> {
                // When the trip is in IDLE state, show both pickup and drop locations on the map
                val pickup = uiState.pickupLocation?.locationPoint
                val drop = uiState.dropLocation?.locationPoint
                zoom(pickup, drop, cameraPositionState, 100)

                if (pickup != null && drop == null) {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(pickup.toLatLng(), 18f),
                        durationMs = 1000
                    )
                }
            }
            else -> { }
        }
    }

    // Local state for the input fields (decoupled from ViewModel until confirmed)
    var pickupInput by rememberSaveable { mutableStateOf("") }
    var dropInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.pickupLocation?.address) {
        pickupInput = uiState.pickupLocation?.address.orEmpty()
    }
    LaunchedEffect(uiState.dropLocation?.address) {
        dropInput = uiState.dropLocation?.address.orEmpty()
    }

    // Animate bottom sheet based on focus state
    LaunchedEffect(uiState.focusedField) {
        coroutineScope.launch {
            if (uiState.focusedField != AddressFieldType.NONE) {
                sheetState.bottomSheetState.expand()
            } else {
                sheetState.bottomSheetState.partialExpand()
            }
        }
    }

    // Default peek height is 50% of screen
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val peekHeight = screenHeight * uiState.tripState.peekHeight()

    // Root container
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BottomSheetScaffold(
            scaffoldState = sheetState,
            sheetPeekHeight = peekHeight,
            modifier = Modifier.imePadding(),
            sheetContainerColor = Color(0xFFF2F1F4),
            sheetContent = {
                RideBottomSheetContent(
                    uiState = uiState,
                    pickupInput = pickupInput,
                    dropInput = dropInput,
                    onPickupChange = {
                        pickupInput = it
                        viewModel.fetchAddressPredictions(it, isPickup = true)
                    },
                    onDropChange = {
                        dropInput = it
                        viewModel.fetchAddressPredictions(it, isPickup = false)
                    },
                    onFieldFocusChanged = {
                        viewModel.updateFocusedField(it)
                    },
                    onSuggestionSelected = { prediction ->
                        val isPickup = uiState.focusedField == AddressFieldType.PICKUP
                        viewModel.selectPlace(prediction.placeId, isPickup)
                        viewModel.updateFocusedField(AddressFieldType.NONE)
                        focusManager.clearFocus()
                    },
                    onVehicleSelected = { viewModel.updateSelectedVehicle(it) }
                )
            }
        ) {
            GoogleMapView(
                pickupLocation = uiState.pickupLocation?.locationPoint,
                dropLocation = uiState.dropLocation?.locationPoint,
                carPosition = uiState.carPosition,
                routePolyline = uiState.routePolyline,
                tripState = uiState.tripState,
                cameraPositionState = cameraPositionState,
                isPermissionGranted = uiState.isPermissionGranted,
                selectedVehicle = uiState.selectedVehicle,
                carRotation = uiState.carRotation
            )
            LaunchedEffect(uiState.carPosition) {
                if (uiState.tripState.shouldFollowCar()) {
                    uiState.carPosition?.let { carLatLng ->
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLng(carLatLng.toLatLng()),
                            durationMs = 180
                        )
                    }
                }
            }
        }

        if (uiState.isRideBookingReady()) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BookRideSection(191.0) { viewModel.startRideSimulation() }
            }
        }
    }
}

private suspend fun zoom(
    pickup: LatLngPoint?,
    drop: LatLngPoint?,
    cameraPositionState: CameraPositionState,
    padding: Int
) {
    if (pickup != null && drop != null) {
        val bounds = LatLngBounds.builder()
            .include(pickup.toLatLng())
            .include(drop.toLatLng())
            .build()

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngBounds(bounds, padding),
            durationMs = 1000
        )
    }
}
