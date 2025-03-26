package com.an.ridesim.ui.viewmodel

import com.an.ridesim.data.PlacesRepository
import com.an.ridesim.data.RouteRepository
import com.google.android.libraries.places.api.model.AutocompletePrediction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.ridesim.model.*
import com.an.ridesim.util.FareCalculator
import com.an.ridesim.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [RideViewModel] manages the entire business logic and UI state
 * for the RideSim ride-hailing simulation app.
 *
 * It connects the UI to:
 * - Location utilities
 * - Places API
 * - Directions API
 * - Fare calculation
 * - Trip simulation (driver movement)
 */

@HiltViewModel
class RideViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val routeRepository: RouteRepository,
    private val locationUtils: LocationUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideUiState(
        isPermissionGranted = locationUtils.hasLocationPermission()
    ))
    val uiState: StateFlow<RideUiState> = _uiState.asStateFlow()

    fun updatePermissionState(value: Boolean) {
        _uiState.update {
            it.copy(
                isPermissionGranted = value
            )
        }
    }

    /**
     * Fetches the device's last known location using LocationUtils.
     * If available, sets it as the pickup point in the UI.
     */
    fun fetchCurrentLocationAsPickup() {
        viewModelScope.launch {
            try {
                val location = locationUtils.getLastKnownLocation()
                location?.let { point ->
                    _uiState.update {
                        it.copy(
                            pickupLocation = point,
                            pickupAddress = "Current Location"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(locationError = e.message ?: "Location unavailable") }
            }
        }
    }

    /**
     * Called when the user types in the pickup or drop text field.
     * Fetches location suggestions using the Places API.
     */
    fun fetchAddressPredictions(query: String, isPickup: Boolean) {
        viewModelScope.launch {
            try {
                val results = placesRepository.getPlacePredictions(query)
                _uiState.update {
                    if (isPickup) it.copy(pickupSuggestions = results)
                    else it.copy(dropSuggestions = results)
                }
            } catch (_: Exception) {
                // Silent failure – optionally handle UI error state
            }
        }
    }

    /**
     * Called when the user selects a prediction from suggestions.
     * Converts Place ID to full address and lat/lng using Places API.
     * Updates either pickup or drop location and triggers route + fare calculation.
     */
    fun selectPlace(placeId: String, isPickup: Boolean) {
        viewModelScope.launch {
            try {
                val (latLng, address) = placesRepository.resolvePlaceId(placeId) ?: return@launch
                val latLngPoint = LatLngPoint(latLng.latitude, latLng.longitude)

                _uiState.update {
                    if (isPickup) it.copy(
                        pickupLocation = latLngPoint,
                        pickupAddress = address,
                        pickupSuggestions = emptyList()
                    ) else it.copy(
                        dropLocation = latLngPoint,
                        dropAddress = address,
                        dropSuggestions = emptyList()
                    )
                }

                calculateRouteAndFare()
            } catch (e: Exception) {
                _uiState.update { it.copy(locationError = e.message ?: "Place resolution failed") }
            }
        }
    }

    /**
     * Fetches the route polyline, distance and duration between pickup and drop points
     * using the Directions API (wrapped via RouteRepository).
     * Also triggers fare calculation.
     */
    private fun calculateRouteAndFare() {
        val pickup = _uiState.value.pickupLocation
        val drop = _uiState.value.dropLocation
        if (pickup == null || drop == null) return

        viewModelScope.launch {
            try {
                val result = routeRepository.getRoute(pickup.toLatLng(), drop.toLatLng())
                result?.let {
                    _uiState.update {
                        it.copy(
                            distanceInKm = result.distanceInKm,
                            durationInMinutes = result.durationInMinutes.toInt(),
                            routePolyline = result.routePoints.map { pt ->
                                LatLng(pt.latitude, pt.longitude)
                            }
                        )
                    }
                    calculateFare()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(routeError = e.message ?: "Route fetch failed") }
            }
        }
    }

    /**
     * Calculates the fare using [FareCalculator], based on selected vehicle,
     * distance, and duration.
     */
    private fun calculateFare() {
        val state = _uiState.value
        if (state.distanceInKm != null && state.durationInMinutes != null) {
            val fare = FareCalculator.calculateFare(
                distanceInKm = state.distanceInKm,
                waitTimeInMinutes = state.durationInMinutes,
                vehicleType = state.selectedVehicle
            )
            _uiState.update { it.copy(estimatedFare = fare) }
        }
    }

    /**
     * Updates the selected vehicle type and triggers a fare recalculation.
     */
    fun updateVehicleType(vehicleType: VehicleType) {
        _uiState.update { it.copy(selectedVehicle = vehicleType) }
        calculateFare()
    }

    /**
     * Starts the trip simulation flow:
     * - DRIVER_ARRIVING
     * - ON_TRIP
     * - COMPLETED
     *
     * Animates the car position along the polyline.
     */
    fun startRideSimulation() {
        viewModelScope.launch {
            _uiState.update { it.copy(tripState = TripState.DRIVER_ARRIVING) }

            delay(3000)

            _uiState.update { it.copy(tripState = TripState.ON_TRIP) }

            val path = _uiState.value.routePolyline
            for (point in path) {
                delay(500)
                _uiState.update { it.copy(carPosition = LatLngPoint(point.latitude, point.longitude)) }
            }

            _uiState.update {
                it.copy(tripState = TripState.COMPLETED, carPosition = null)
            }
        }
    }

    /**
     * Resets the trip state and car position.
     */
    fun resetSimulation() {
        _uiState.update {
            it.copy(
                tripState = TripState.IDLE,
                estimatedFare = null,
                carPosition = null
            )
        }
    }

    /**
     * Full UI state for the RideSim screen.
     */
    data class RideUiState(
        val isPermissionGranted: Boolean,
        val pickupAddress: String? = null,
        val pickupLocation: LatLngPoint? = null,
        val dropAddress: String? = null,
        val dropLocation: LatLngPoint? = null,
        val pickupSuggestions: List<AutocompletePrediction> = emptyList(),
        val dropSuggestions: List<AutocompletePrediction> = emptyList(),
        val selectedVehicle: VehicleType = VehicleType.AUTO,
        val estimatedFare: Int? = null,
        val distanceInKm: Double? = null,
        val durationInMinutes: Int? = null,
        val tripState: TripState = TripState.IDLE,
        val routePolyline: List<LatLng> = emptyList(),
        val carPosition: LatLngPoint? = null,
        val locationError: String? = null,
        val routeError: String? = null
    )
}
