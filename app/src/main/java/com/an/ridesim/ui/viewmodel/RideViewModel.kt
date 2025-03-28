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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.an.ridesim.R

/**
 * [RideViewModel] manages all business logic and UI state for the RideSim app.
 *
 * Responsibilities:
 * - Request and reverse geocode current location
 * - Fetch address predictions from Places API as the user types
 * - Handle selection of predicted addresses
 * - Trigger route and fare calculations
 * - Manage ride simulation (driver movement)
 * - Handle UI state transitions and screen data flow
 */

@HiltViewModel
class RideViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val routeRepository: RouteRepository,
    private val locationUtils: LocationUtils,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideUiState(
        isPermissionGranted = locationUtils.hasLocationPermission()
    ))
    val uiState: StateFlow<RideUiState> = _uiState.asStateFlow()

    /**
     * Updates the location permission state.
     */
    fun updatePermissionState(value: Boolean) {
        _uiState.update {
            it.copy(
                isPermissionGranted = value
            )
        }
    }

    /**
     * Updates which input field is currently focused.
     * This helps determine whether pickup or drop suggestions should be shown.
     */
    fun updateFocusedField(type: AddressFieldType) {
        _uiState.update { it.copy(focusedField = type) }
    }

    /**
     * Fetches the last known device location and reverse geocodes it.
     * Sets it as the default pickup point and address.
     */
    fun fetchCurrentLocationAsPickup() {
        viewModelScope.launch {
            try {
                val location = locationUtils.getLastKnownLocation()
                location?.let { point ->
                    val address = locationUtils.getAddressFromLatLng(point)
                    _uiState.update {
                        it.copy(pickupLocation = point, pickupAddress = address)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(locationError = e.message ?: "Location unavailable") }
            }
        }
    }

    /**
     * Fetches place suggestions from the Google Places API.
     * The results are stored based on whether the user is editing pickup or drop.
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
                // Silent failure - fallback handled by empty suggestions
            }
        }
    }

    /**
     * When a prediction is selected by the user, this fetches the location and full address
     * using the place ID and updates the state accordingly.
     * Also triggers route and fare calculation if both points are available.
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
     * Fetches route between pickup and drop points and calculates ETA.
     * Updates the polyline and time/distance in UI state.
     */
    private fun calculateRouteAndFare() {
        val pickup = _uiState.value.pickupLocation
        val drop = _uiState.value.dropLocation
        if (pickup == null || drop == null) return

        viewModelScope.launch {
            try {
                // 👇 Run network + decoding work in background
                val result = withContext(dispatcher) {
                    routeRepository.getRoute(pickup.toLatLng(), drop.toLatLng())
                }

                result?.let {
                    val polylinePoints = it.routePoints.map { pt -> LatLng(pt.latitude, pt.longitude) }

                    _uiState.update { state ->
                        state.copy(
                            distanceInKm = it.distanceInKm,
                            durationInMinutes = it.durationInMinutes.toInt(),
                            routePolyline = polylinePoints
                        )
                    }

                    // Now compute fare on UI thread
                    calculateFare()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(routeError = e.message ?: "Route fetch failed") }
            }
        }
    }

    /**
     * Calculates fare using selected vehicle type and route metrics.
     */
    private fun calculateFare() {
        val state = _uiState.value
        if (state.distanceInKm != null && state.durationInMinutes != null) {
            val fare = FareCalculator.calculateFare(
                distanceInKm = state.distanceInKm,
                waitTimeInMinutes = state.durationInMinutes,
                vehicleType = state.selectedVehicle
            )
            _uiState.update { it.copy(
                estimatedFare = fare,
                availableVehicles = fetchVehicleDetails()
            ) }
        }
    }

    /**
     * Updates the selected vehicle type and triggers a fare recalculation.
     */
    fun updateVehicleType(vehicleType: VehicleType) {
        _uiState.update { it.copy(selectedVehicle = vehicleType) }
        calculateFare()
    }

    private fun fetchVehicleDetails(): List<VehicleDetail> {
        return listOf(
            VehicleDetail(
                vehicleType = VehicleType.AUTO,
                iconResId = R.drawable.ic_auto,
                displayNameId = R.string.vehicle_auto_name,
                descriptionId = R.string.vehicle_auto_desc,
                peopleCount = R.string.vehicle_auto_count,
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.AUTO
                ).toDouble()
            ),
            VehicleDetail(
                vehicleType = VehicleType.AC_MINI,
                iconResId = R.drawable.ic_mini,
                displayNameId = R.string.vehicle_mini_name,
                descriptionId = R.string.vehicle_mini_desc,
                peopleCount = R.string.vehicle_mini_count,
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.AC_MINI
                ).toDouble()
            ),
            VehicleDetail(
                vehicleType = VehicleType.SEDAN,
                iconResId = R.drawable.ic_sedan,
                displayNameId = R.string.vehicle_sedan_name,
                descriptionId = R.string.vehicle_sedan_desc,
                peopleCount = R.string.vehicle_sedan_count,
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SEDAN
                ).toDouble()
            ),
            VehicleDetail(
                vehicleType = VehicleType.SUV,
                iconResId = R.drawable.ic_suv,
                displayNameId = R.string.vehicle_suv_name,
                descriptionId = R.string.vehicle_suv_desc,
                peopleCount = R.string.vehicle_suv_count,
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SUV
                ).toDouble()
            ),
            VehicleDetail(
                vehicleType = VehicleType.SUV_PLUS,
                iconResId = R.drawable.ic_suv_plus,
                displayNameId = R.string.vehicle_suv_plus_name,
                descriptionId = R.string.vehicle_suv_plus_desc,
                peopleCount = R.string.vehicle_suv_plus_count,
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SUV_PLUS
                ).toDouble()
            )
        )
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
        val routeError: String? = null,
        val focusedField: AddressFieldType = AddressFieldType.NONE,
        val availableVehicles: List<VehicleDetail> = emptyList()
    )
}

/**
 * Enum used to determine which address field is currently focused.
 * Helps the UI know which suggestions to display.
 */
enum class AddressFieldType {
    NONE, PICKUP, DROP
}
