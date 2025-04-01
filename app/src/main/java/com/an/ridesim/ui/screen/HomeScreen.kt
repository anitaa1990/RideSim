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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.ridesim.R
import com.an.ridesim.model.LatLngPoint
import com.an.ridesim.model.TripState
import com.an.ridesim.model.toLatLng
import com.an.ridesim.ui.viewmodel.AddressFieldType
import com.an.ridesim.ui.viewmodel.RideViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
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

        // Animate camera to pickup location
        LaunchedEffect(uiState.pickupLocation) {
            uiState.pickupLocation?.let {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it.toLatLng(), 18f),
                    durationMs = 1000
                )
            }
        }
    }

    // Animate camera to show both pickup & drop when route is available
    LaunchedEffect(uiState.pickupLocation, uiState.dropLocation, uiState.routePolyline) {
        val pickup = uiState.pickupLocation
        val drop = uiState.dropLocation
        val polyline = uiState.routePolyline

        if (pickup != null && drop != null && polyline.isNotEmpty()) {
            val bounds = LatLngBounds.builder()
                .include(pickup.toLatLng())
                .include(drop.toLatLng())
                .build()

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 180),
                durationMs = 1200
            )
        }
    }

    // Local state for the input fields (decoupled from ViewModel until confirmed)
    var pickupInput by rememberSaveable { mutableStateOf("") }
    var dropInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.pickupAddress) {
        pickupInput = uiState.pickupAddress.orEmpty()
    }
    LaunchedEffect(uiState.dropAddress) {
        dropInput = uiState.dropAddress.orEmpty()
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
    val peekHeight = screenHeight * 0.5f

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
                pickupLocation = uiState.pickupLocation,
                dropLocation = uiState.dropLocation,
                carPosition = uiState.carPosition,
                routePolyline = uiState.routePolyline,
                tripState = uiState.tripState,
                cameraPositionState = cameraPositionState,
                isPermissionGranted = uiState.isPermissionGranted
            )
        }

        if (uiState.isRideBookingReady()) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BookRideSection(191.0) { }
            }
        }
    }
}

@Composable
fun GoogleMapView(
    pickupLocation: LatLngPoint?,
    dropLocation: LatLngPoint?,
    carPosition: LatLngPoint?,
    routePolyline: List<LatLng>,
    tripState: TripState,
    cameraPositionState: CameraPositionState,
    isPermissionGranted: Boolean
) {
    val isGradientReversed = when (tripState) {
        TripState.DRIVER_ARRIVING -> true
        TripState.ON_TRIP -> false
        else -> false
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isPermissionGranted
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = isPermissionGranted
        )
    ) {
        val context = LocalContext.current

        // Marker: Pickup point
        pickupLocation?.let {
            val pickupIcon = remember {
                BitmapDescriptorFactory.fromBitmap(
                    ContextCompat.getDrawable(context, R.drawable.ic_pickup_marker)!!.toBitmap()
                )
            }
            Marker(state = MarkerState(
                position = it.toLatLng()),
                icon = pickupIcon,
                anchor = Offset(0.5f, 1.2f)
            )
        }

        // Marker: Drop
        dropLocation?.let {
            val dropIcon = remember {
                BitmapDescriptorFactory.fromBitmap(
                    ContextCompat.getDrawable(context, R.drawable.ic_drop_flag_marker)!!.toBitmap()
                )
            }
            Marker(
                state = MarkerState(position = it.toLatLng()),
                icon = dropIcon,
                anchor = Offset(0.5f, 1.2f)
            )
        }

        // Marker: Moving car during simulation
        carPosition?.let {
            Marker(state = MarkerState(position = it.toLatLng()))
        }

        // Polyline: Route between pickup & drop
        // Polyline: Route between pickup & drop
        if (routePolyline.size >= 2) {
            // Iterate over each segment between consecutive points in the polyline
            for (i in 0 until routePolyline.lastIndex) {

                // Calculate the position of this segment relative to the whole path
                // For example, if there are 10 segments, `fraction` will go from 0.0 to 1.0
                val fraction = i.toFloat() / routePolyline.lastIndex

                // If gradient is reversed (e.g., vehicle approaching pickup), invert the gradient direction
                val adjustedFraction = if (isGradientReversed) 1f - fraction else fraction

                // Interpolate the color for this segment based on its relative position
                val color = lerpColor(
                    startColor = Color(0xFFFAC901).toArgb(), // Bright Yellow (Pickup end)
                    endColor = Color.Black.toArgb(),         // Black (Drop end)
                    fraction = adjustedFraction              // Adjusted to allow reversing
                )

                // Draw the current polyline segment with the computed gradient color
                Polyline(
                    points = listOf(routePolyline[i], routePolyline[i + 1]), // This segment
                    color = Color(color),                                     // Gradient segment color
                    width = 10f                                               // Line thickness
                )
            }
        }
    }
}

/**
 * Linearly interpolates (lerps) between two colors based on a given [fraction].
 * Used to create smooth gradients between a start and end color.
 *
 * @param startColor The ARGB color to start from (Int representation).
 * @param endColor The ARGB color to interpolate to (Int representation).
 * @param fraction A value between 0.0 and 1.0 representing interpolation progress.
 *                 0.0 = startColor, 1.0 = endColor, 0.5 = halfway blend.
 *
 * @return The interpolated color as a packed ARGB Int.
 */
private fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
    // Decompose startColor into alpha, red, green, blue (8-bit components)
    val startA = (startColor shr 24) and 0xff // alpha
    val startR = (startColor shr 16) and 0xff // red
    val startG = (startColor shr 8) and 0xff  // green
    val startB = startColor and 0xff          // blue

    // Decompose endColor into alpha, red, green, blue (8-bit components)
    val endA = (endColor shr 24) and 0xff
    val endR = (endColor shr 16) and 0xff
    val endG = (endColor shr 8) and 0xff
    val endB = endColor and 0xff

    // Linearly interpolate each channel using the formula:
    // result = start + ((end - start) * fraction)
    val a = (startA + ((endA - startA) * fraction)).toInt()
    val r = (startR + ((endR - startR) * fraction)).toInt()
    val g = (startG + ((endG - startG) * fraction)).toInt()
    val b = (startB + ((endB - startB) * fraction)).toInt()

    // Recombine the channels into a single ARGB Int:
    // (alpha << 24) | (red << 16) | (green << 8) | blue
    return (a and 0xff shl 24) or
            (r and 0xff shl 16) or
            (g and 0xff shl 8) or
            (b and 0xff)
}
