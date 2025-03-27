package com.an.ridesim.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.ridesim.R
import com.an.ridesim.model.LatLngPoint
import com.an.ridesim.model.toLatLng
import com.an.ridesim.ui.viewmodel.RideViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * [HomeScreen] is the main UI Composable for the RideSim ride-booking experience.
 *
 * It serves as the entry point of the app where users can:
 * - View a map centered around their current location
 * - Enter pickup and drop addresses
 * - See autocomplete suggestions as they type (via Places API)
 * - View route on the map (polyline) after selection
 * - See their ride simulation in progress
 *
 * 🧠 Architecture:
 * - It connects to [RideViewModel] via [StateFlow] to read/write state.
 * - Relies on the Google Maps Compose library to render the map.
 * - Uses Jetpack Compose UI elements for input and display.
 *
 * 🧰 Internally, it handles:
 * - Camera movement as user location is set
 * - Prediction fetching and display as user types
 * - Real-time updates to the map (e.g., polyline and driver marker)
 *
 * This screen mimics modern ride-hailing apps (like Uber/Ola) by combining
 * live map interactivity and address input into a clean bottom-sheet UI.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RideViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        lifecycleOwner = LocalLifecycleOwner.current
    )

    val cameraPositionState = rememberCameraPositionState()

    // Request location permission
    if (!uiState.isPermissionGranted) {
        RequestPermissionScreen(onPermissionChanged = {
            viewModel.updatePermissionState(it)
        })
    } else {
        viewModel.fetchCurrentLocationAsPickup()
        // Animate camera to pickup location (on launch or update)
        LaunchedEffect(uiState.pickupLocation) {
            uiState.pickupLocation?.let {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it.toLatLng(), 18f),
                    durationMs = 1000
                )
            }
        }
    }

    // Bottom sheet scaffold state (for controlling the sheet's expansion/collapse)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Map UI
    Box(modifier = Modifier.fillMaxSize()) {
        // 1️⃣ Map view using Google Maps Compose
        GoogleMapView(
            pickupLocation = uiState.pickupLocation,
            dropLocation = uiState.dropLocation,
            carPosition = uiState.carPosition,
            routePolyline = uiState.routePolyline,
            cameraPositionState = cameraPositionState,
            isPermissionGranted = uiState.isPermissionGranted
        )

        // Ride Bottom Sheet UI
        ModalBottomSheet(
            containerColor = Color(0XFFF2F1F4),
            modifier = Modifier.fillMaxHeight(0.6f),
            sheetState = sheetState,
            onDismissRequest = {  }
        ) {
            RideBottomSheetContent(
                pickupText = uiState.pickupAddress ?: "",
                dropText = uiState.dropAddress ?: "",
                onPickupChange = { },
                onDropChange = { }
            )
        }
    }
}

@Composable
fun GoogleMapView(
    pickupLocation: LatLngPoint?,
    dropLocation: LatLngPoint?,
    carPosition: LatLngPoint?,
    routePolyline: List<LatLng>,
    cameraPositionState: CameraPositionState,
    isPermissionGranted: Boolean
) {
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
        // Marker: Pickup point
        pickupLocation?.let {
            val context = LocalContext.current
            val greenPinIcon = remember {
                BitmapDescriptorFactory.fromBitmap(
                    ContextCompat.getDrawable(context, R.drawable.ic_drop_marker)!!.toBitmap()
                )
            }
            Marker(state = MarkerState(
                position = it.toLatLng()),
                icon = greenPinIcon,
                anchor = Offset(0.5f, 1.2f)
            )
        }

        // Marker: Drop point
        dropLocation?.let {
            Marker(state = MarkerState(position = it.toLatLng()))
        }

        // Marker: Moving car during simulation
        carPosition?.let {
            Marker(state = MarkerState(position = it.toLatLng()))
        }

        // Polyline: Route between pickup & drop
        if (routePolyline.isNotEmpty()) {
            Polyline(
                points = routePolyline,
                color = Color.Blue,
                width = 10f
            )
        }
    }
}
