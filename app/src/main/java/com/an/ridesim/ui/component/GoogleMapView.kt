package com.an.ridesim.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.an.ridesim.R
import com.an.ridesim.model.LatLngPoint
import com.an.ridesim.model.TripState
import com.an.ridesim.model.VehicleDetail
import com.an.ridesim.model.toLatLng
import com.an.ridesim.util.MapUtils
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun GoogleMapView(
    pickupLocation: LatLngPoint?,
    dropLocation: LatLngPoint?,
    carPosition: LatLngPoint?,
    routePolyline: List<LatLng>,
    tripState: TripState,
    cameraPositionState: CameraPositionState,
    isPermissionGranted: Boolean,
    selectedVehicle: VehicleDetail,
    carRotation: Float?
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
            // Get the vehicle marker icon based on the selected vehicle type
            val vehicleIcon = remember(selectedVehicle) {
                MapUtils.getResizedIconWithAspectRatio(selectedVehicle.markerIconResId, context, 70)
            }
            val carMarkerState = rememberUpdatedMarkerState(position = it.toLatLng())
            Marker(
                state = carMarkerState,
                icon = vehicleIcon,
                rotation = carRotation ?: 0f,
                anchor = Offset(0.5f, 0.5f),
                flat = true // required to rotate icon
            )
        }

        // Polyline: Route between pickup & drop
        if (routePolyline.size >= 2) {
            // Iterate over each segment between consecutive points in the polyline
            for (i in 0 until routePolyline.lastIndex) {

                // Calculate the position of this segment relative to the whole path
                // For example, if there are 10 segments, `fraction` will go from 0.0 to 1.0
                val fraction = i.toFloat() / routePolyline.lastIndex

                // If gradient is reversed (e.g., vehicle approaching pickup),
                // invert the gradient direction
                val adjustedFraction = if (isGradientReversed) 1f - fraction else fraction

                // Interpolate the color for this segment based on its relative position
                val color = MapUtils.lerpColor(
                    startColor = Color(0xFFFAC901).toArgb(), // Bright Yellow (Pickup end)
                    endColor = Color.Black.toArgb(),         // Black (Drop end)
                    fraction = adjustedFraction              // Adjusted to allow reversing
                )

                // Draw the current polyline segment with the computed gradient color
                Polyline(
                    points = listOf(routePolyline[i], routePolyline[i + 1]), // This segment
                    color = Color(color),   // Gradient segment color
                    width = 10f // Line thickness
                )
            }
        }
    }
}
