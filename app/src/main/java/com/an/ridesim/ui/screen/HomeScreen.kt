package com.an.ridesim.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.an.ridesim.ui.viewmodel.RideViewModel

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

@Composable
fun HomeScreen(
    viewModel: RideViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(
        lifecycleOwner = LocalLifecycleOwner.current
    )

    // TODO: Request location permission
}
