package com.an.ridesim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.an.ridesim.data.PlacesRepository
import com.an.ridesim.data.RouteRepository
import com.an.ridesim.model.*
import com.an.ridesim.ui.model.LocationUiModel
import com.an.ridesim.ui.model.RideUiModel
import com.an.ridesim.util.FareCalculator
import com.an.ridesim.util.LocationUtils
import com.an.ridesim.util.MapUtils
import com.an.ridesim.util.RideUtils
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
import com.an.ridesim.ui.model.RideStatusUiModel

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
                    val ( address, area) = locationUtils.getAddressFromLatLng(point)
                    _uiState.update {
                        it.copy(
                            pickupLocation = LocationUiModel(
                                address = address,
                                subLocality = area,
                                locationPoint = point
                            )
                        )
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
                val (latLng, address, area) = placesRepository.resolvePlaceId(placeId) ?: return@launch
                val latLngPoint = LatLngPoint(latLng.latitude, latLng.longitude)

                _uiState.update {
                    if (isPickup) it.copy(
                        pickupLocation = LocationUiModel(address, area, latLngPoint),
                        pickupSuggestions = emptyList()
                    ) else it.copy(
                        dropLocation = LocationUiModel(address, area, latLngPoint),
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
        val pickup = _uiState.value.pickupLocation?.locationPoint
        val drop = _uiState.value.dropLocation?.locationPoint
        if (pickup == null || drop == null) return

        viewModelScope.launch {
            try {
                // 👇 Run network + decoding work in background
                val result = withContext(dispatcher) {
                    routeRepository.getRoute(pickup.toLatLng(), drop.toLatLng())
                }

                result?.let {
                    val polylinePoints = it.routePoints.map {
                        pt -> LatLng(pt.latitude, pt.longitude)
                    }
                    val vehicleDetails = fetchVehicleDetails()

                    _uiState.update { state ->
                        state.copy(
                            rideUiModel = RideUiModel(
                                rideId = RideUtils.generateRandomRideId(),
                                driverName = RideUtils.getRandomDriverName(),
                                distanceInKm = it.distanceInKm,
                                durationInMinutes = it.durationInMinutes.toInt(),
                                rideStartTimeString = RideUtils.getRideTimeFormatted(),
                                otp = RideUtils.generateSixDigitOtp()
                            ),
                            routePolyline = polylinePoints,
                            availableVehicles = vehicleDetails,
                            selectedVehicle = vehicleDetails.first()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(routeError = e.message ?: "Route fetch failed") }
            }
        }
    }

    /**
     * Updates the selected vehicle type and triggers a fare recalculation.
     */
    fun updateSelectedVehicle(selectedVehicle: VehicleDetail) {
        _uiState.update { it.copy(selectedVehicle = selectedVehicle) }
    }

    private fun fetchVehicleDetails(): List<VehicleDetail> {
        val rideUiModel = _uiState.value.rideUiModel
        return listOf(
            VehicleDetail.getAuto().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = rideUiModel.distanceInKm ?: 0.0,
                    waitTimeInMinutes = rideUiModel.durationInMinutes ?: 0,
                    vehicleType = VehicleType.AUTO
                ).toDouble()
            ),
            VehicleDetail.getMini().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = rideUiModel.distanceInKm ?: 0.0,
                    waitTimeInMinutes = rideUiModel.durationInMinutes ?: 0,
                    vehicleType = VehicleType.AC_MINI
                ).toDouble()
            ),
            VehicleDetail.getSedan().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = rideUiModel.distanceInKm ?: 0.0,
                    waitTimeInMinutes = rideUiModel.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SEDAN
                ).toDouble()
            ),
            VehicleDetail.getSUV().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = rideUiModel.distanceInKm ?: 0.0,
                    waitTimeInMinutes = rideUiModel.durationInMinutes ?: 0,
                    vehicleType = VehicleType.SUV
                ).toDouble()
            ),
            VehicleDetail.getSUVPlus().copy(
                price = FareCalculator.calculateFare(
                    distanceInKm = rideUiModel.distanceInKm ?: 0.0,
                    waitTimeInMinutes = rideUiModel.durationInMinutes ?: 0,
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
            val pickup = _uiState.value.pickupLocation?.locationPoint ?: return@launch
            val randomStartPoint = MapUtils.generateRandomStartPoint(pickup, 1000.0, 2000.0)
            _uiState.update { it.copy(carPosition = randomStartPoint) }

            // Step 3: Calculate the route from the random starting point to the pickup location
            // The updateRouteInfo method will fetch and set the route between start and pickup
            updateRouteInfo(randomStartPoint, pickup)

            // Step 4: Simulate vehicle movement from the random start point to the pickup location.
            // This function updates the car's position and polyline during the movement
            simulateVehicleMovement(targetLocation = pickup)

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

            delay(2000)

            // Step 5: Once the driver arrives at the pickup, change the trip state to ON_TRIP
            // After the vehicle reaches the pickup location, the trip transitions to the
            // "ON_TRIP" state
            _uiState.update { it.copy(tripState = TripState.ON_TRIP) }

            // Step 6: Now simulate the trip from the pickup location to the drop location
            // // Ensure drop location is available
            val drop = _uiState.value.dropLocation?.locationPoint ?: return@launch

            // Fetch and update the route between pickup and drop
            updateRouteInfo(pickup, drop)

            // Step 7: Simulate vehicle movement from the pickup to the drop location
            // Update vehicle's position and polyline during the trip to the drop
            simulateVehicleMovement(targetLocation = drop)

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
                pickupLocation = null,
                dropLocation = null,
                pickupSuggestions = emptyList(),
                dropSuggestions = emptyList(),
                tripState = TripState.IDLE,
                carPosition = null,
                selectedVehicle = VehicleDetail.getAuto(),
                rideUiModel = RideUiModel(),
                routePolyline = emptyList(),
                locationError = null,
                routeError = null,
                focusedField = AddressFieldType.NONE,
                availableVehicles = emptyList(),
                carRotation = null
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
     * Simulates the vehicle's movement along the route polyline with smooth interpolation
     * and rotation.
     *
     * This method animates the car marker along the list of coordinates in `routePolyline`
     * to simulate real-time driving. It also interpolates the car's bearing (rotation) and applies
     * easing curves to ensure natural motion, mimicking real ride-hailing apps like Uber or Ola.
     */
    private suspend fun simulateVehicleMovement(
        targetLocation: LatLngPoint
    ) {
        val path = _uiState.value.routePolyline

        // Cannot simulate with less than 2 points
        if (path.size < 2) return

        var currentIndexTrip = 0

        // Loop through each consecutive pair of points in the route
        for (i in 0 until path.lastIndex) {
            val start = path[i]
            val end = path[i + 1]

            val startPoint = LatLngPoint(start.latitude, start.longitude)
            val endPoint = LatLngPoint(end.latitude, end.longitude)

            // Current rotation of the vehicle; default to 0° if null
            val startRotation = _uiState.value.carRotation ?: 0f

            // Target rotation states the bearing from start to end in degrees
            val targetRotation = MapUtils.computeBearing(startPoint, endPoint)

            // Calculate distance of this segment to dynamically determine animation speed
            val distanceMeters = locationUtils.calculateHaversineDistance(startPoint, endPoint)

            // Simulated time to cover this segment (assuming ~40 km/h speed)
            val durationMs = ((distanceMeters / 1000.0) / 40.0) * 3600_000

            // We'll break this segment into N steps for smooth interpolation
            val steps = 30
            val stepDuration = (durationMs / steps).coerceIn(10.0, 100.0).toLong()

            // Interpolate car position + bearing across the steps
            for (step in 1..steps) {
                val t = step.toFloat() / steps

                // Apply easing function for smoothness
                val easedT = MapUtils.EaseInOutCubic(t)

                // Interpolated position (lat/lng) between start and end
                val lat = start.latitude + (end.latitude - start.latitude) * easedT
                val lng = start.longitude + (end.longitude - start.longitude) * easedT

                // New car position
                val newPosition = LatLngPoint(lat, lng)

                // Interpolated rotation between start and target
                val interpolatedRotation = MapUtils.lerpAngle(startRotation, targetRotation, easedT)

                // Calculate distance between current car position and target location
                val distanceToTarget = locationUtils.calculateHaversineDistance(newPosition, targetLocation)

                // Update the UI state with new car position and rotation
                _uiState.update {
                    it.copy(
                        carPosition = LatLngPoint(lat, lng),
                        carRotation = interpolatedRotation,
                        rideStatusUiModel = RideStatusUiModel(
                            hasDriverArrived = distanceToTarget < 0.05,
                            distanceToTarget = distanceToTarget
                        )
                    )
                }

                // Wait before next frame to simulate smooth animation
                delay(stepDuration)
            }

            // Remove the segment that was just traversed to shrink the remaining route
            if (i > currentIndexTrip) {
                val updatedPolyline = path.subList(i + 1, path.size)
                _uiState.update { it.copy(routePolyline = updatedPolyline) }
                currentIndexTrip = i
            }
        }
    }

    /**
     * Full UI state for the RideSim screen.
     */
    data class RideUiState(
        val isPermissionGranted: Boolean,
        val pickupLocation: LocationUiModel? = null,
        val dropLocation: LocationUiModel? = null,
        val pickupSuggestions: List<AutocompletePrediction> = emptyList(),
        val dropSuggestions: List<AutocompletePrediction> = emptyList(),
        val selectedVehicle: VehicleDetail = VehicleDetail.getAuto(),
        val rideUiModel: RideUiModel = RideUiModel(),
        val tripState: TripState = TripState.IDLE,
        val routePolyline: List<LatLng> = emptyList(),
        val carPosition: LatLngPoint? = null,
        val locationError: String? = null,
        val routeError: String? = null,
        val focusedField: AddressFieldType = AddressFieldType.NONE,
        val availableVehicles: List<VehicleDetail> = emptyList(),
        val carRotation: Float? = null,
        val rideStatusUiModel: RideStatusUiModel = RideStatusUiModel()
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
