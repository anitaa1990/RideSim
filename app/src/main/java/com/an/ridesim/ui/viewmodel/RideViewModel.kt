package com.an.ridesim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.ridesim.data.PlacesRepository
import com.an.ridesim.data.RouteRepository
import com.an.ridesim.model.*
import com.an.ridesim.util.FareCalculator
import com.an.ridesim.util.LocationUtils
import com.an.ridesim.util.MapUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
                vehicleType = state.selectedVehicle.vehicleType
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
    fun updateSelectedVehicle(selectedVehicle: VehicleDetail) {
        _uiState.update { it.copy(selectedVehicle = selectedVehicle) }
        calculateFare()
    }

    private fun fetchVehicleDetails(): List<VehicleDetail> {
        return listOf(
            VehicleDetail.getAuto().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.AUTO
                ).toDouble()
            ),
            VehicleDetail.getMini().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.AC_MINI
                ).toDouble()
            ),
            VehicleDetail.getSedan().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SEDAN
                ).toDouble()
            ),
            VehicleDetail.getSUV().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SUV
                ).toDouble()
            ),
            VehicleDetail.getSUVPlus().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = _uiState.value.distanceInKm ?: 0.0,
                    waitTimeInMinutes = _uiState.value.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SUV_PLUS
                ).toDouble()
            )
        )
    }

    /**
     * Simulates the ride from the initial random start point to the pickup location, and then
     * from the pickup location to the drop location. The simulation updates the trip state and
     * vehicle position on the map. The trip state changes as the vehicle moves through different
     * stages: DRIVER_ARRIVING, ON_TRIP, and COMPLETED.
     */
    fun startRideSimulation() {
        viewModelScope.launch {
            // Step 1: Start the trip with the DRIVER_ARRIVING state
            _uiState.update { it.copy(tripState = TripState.DRIVER_ARRIVING) }

            // Step 2: Randomly set initial position (within 1-2km of the pickup location)
            // This sets the initial random location for the vehicle within a defined distance range
            val pickup = _uiState.value.pickupLocation ?: return@launch
            val randomStartPoint = MapUtils.generateRandomStartPoint(pickup, 1000.0, 2000.0)
            _uiState.update { it.copy(carPosition = randomStartPoint) }

            // Step 3: Calculate the route from the random starting point to the pickup location
            // The updateRouteInfo method will fetch and set the route between start and pickup
            updateRouteInfo(randomStartPoint, pickup)

            // Step 4: Simulate vehicle movement from the random start point to the pickup location.
            // This function updates the car's position and polyline during the movement
            simulateVehicleMovement()

            // Initialize the polyline correctly from the driver's initial position to the
            // pickup location. Ensure that the polyline is set correctly the first time with
            // the initial vehicle position
            val driverStart = _uiState.value.carPosition ?: return@launch

            // Set the initial polyline from the driver's location to the pickup location
            if (_uiState.value.routePolyline.isEmpty()) {
                // Create the initial polyline between the driver's location and pickup location
                val initialPolyline = listOf(
                    LatLng(driverStart.latitude, driverStart.longitude),
                    LatLng(pickup.latitude, pickup.longitude)
                )

                // Update the routePolyline in the state
                _uiState.update { it.copy(routePolyline = initialPolyline) }
            }


            // Step 5: Once the driver arrives at the pickup, change the trip state to ON_TRIP
            // After the vehicle reaches the pickup location, the trip transitions to the
            // "ON_TRIP" state
            _uiState.update { it.copy(tripState = TripState.ON_TRIP) }

            // Step 6: Now simulate the trip from the pickup location to the drop location
            // // Ensure drop location is available
            val drop = _uiState.value.dropLocation ?: return@launch

            // Fetch and update the route between pickup and drop
            updateRouteInfo(pickup, drop)

            // Step 7: Simulate vehicle movement from the pickup to the drop location
            // Update vehicle's position and polyline during the trip to the drop
            simulateVehicleMovement()

            // Step 8: Once the vehicle has arrived at the drop-off location,
            // update the trip state to COMPLETED. Get the final point on the route
            val finalPoint = _uiState.value.routePolyline.last().toLatLngPoint()

            // Calculate the distance to the drop-off location
            val distanceToDrop = locationUtils.calculateHaversineDistance(finalPoint, drop)

            // If the vehicle is within 50 meters of the drop-off location,
            // mark the trip as completed
            if (distanceToDrop < 50) {  // If within 100 meters of drop location
                _uiState.update {
                    // Update the trip state to COMPLETED
                    it.copy(tripState = TripState.COMPLETED)
                }
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
     * Calculates and updates the route (polyline) between the pickup and drop-off locations.
     * The method fetches the route from the route repository and updates the routePolyline
     * in the UI state, which is used to display the path on the map.
     *
     * @param pickup The pickup location represented as a [LatLngPoint].
     * @param drop The drop-off location represented as a [LatLngPoint].
     *
     */
    private suspend fun updateRouteInfo(
        pickup: LatLngPoint,
        drop: LatLngPoint
    ) {
        // Fetch the route information from the repository between the pickup and drop locations
        val routeResultTrip = withContext(dispatcher) {
            // Convert the route points to a list of [LatLng] to represent the polyline
            routeRepository.getRoute(pickup.toLatLng(), drop.toLatLng())
        }

        // Update the UI state with the new polyline for rendering on the map.
        routeResultTrip?.let {
            val pickupToDropPolyline = it.routePoints.map { pt ->
                LatLng(pt.latitude, pt.longitude)
            }
            _uiState.update { it.copy(routePolyline = pickupToDropPolyline) }
        }
    }

    /**
     * Simulates the vehicle's movement along the route polyline.
     * The method updates the vehicle's position on the map by iterating through the route polyline
     * points. It also updates the polyline, removing segments that the vehicle has already crossed.
     *
     */
    private suspend fun simulateVehicleMovement() {
        val driverPath = _uiState.value.routePolyline
        var currentIndexTrip = 0

        // Loop through the route points (driver path) to simulate the movement
        for (i in driverPath.indices) {
            // Introduce a delay between each movement to create a smooth animation effect
            delay(500)

            // For each point in the route, update the vehicle's position on the map
            val point = driverPath[i]
            _uiState.update { it.copy(carPosition = LatLngPoint(point.latitude, point.longitude)) }

            // Remove the polyline segments that the vehicle has already crossed,
            // making the polyline dynamic
            if (i > currentIndexTrip) {
                val updatedPolyline = driverPath.subList(i, driverPath.size)
                _uiState.update { it.copy(routePolyline = updatedPolyline) }
                currentIndexTrip = i  // Update the current index
            }
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
        val selectedVehicle: VehicleDetail = VehicleDetail.getAuto(),
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
    ) {
        fun isRideBookingReady() = tripState == TripState.IDLE &&
                pickupLocation != null
                && dropLocation != null
                && routePolyline.isNotEmpty()
    }
}

/**
 * Enum used to determine which address field is currently focused.
 * Helps the UI know which suggestions to display.
 */
enum class AddressFieldType {
    NONE, PICKUP, DROP
}
