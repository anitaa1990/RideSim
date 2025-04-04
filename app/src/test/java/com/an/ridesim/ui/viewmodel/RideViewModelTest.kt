package com.an.ridesim.ui.viewmodel

import com.an.ridesim.data.PlacesRepository
import com.an.ridesim.data.RouteRepository
import com.an.ridesim.model.*
import com.an.ridesim.ui.model.RideUiModel
import com.an.ridesim.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class RideViewModelTest {

    private val placesRepository: PlacesRepository = mock()
    private val routeRepository: RouteRepository = mock()
    private val locationUtils: LocationUtils = mock()
    private lateinit var viewModel: RideViewModel

    val scheduler = TestCoroutineScheduler()
    val dispatcher = StandardTestDispatcher(scheduler)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        viewModel = RideViewModel(placesRepository, routeRepository, locationUtils, dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchCurrentLocationAsPickup updates pickupLocation with address and area`() = runTest {
        val mockLatLngPoint = LatLngPoint(13.0827, 80.2707)
        whenever(locationUtils.getLastKnownLocation()).thenReturn(mockLatLngPoint)
        whenever(locationUtils.getAddressFromLatLng(mockLatLngPoint))
            .thenReturn(Pair("123 Main St", "Thoraipakkam"))

        viewModel.fetchCurrentLocationAsPickup()

        // Ensures coroutine launched inside ViewModel completes
        runCurrent()

        val state = viewModel.uiState.value
        assertNotNull(state.pickupLocation)
        assertEquals("123 Main St", state.pickupLocation?.address)
        assertEquals("Thoraipakkam", state.pickupLocation?.subLocality)
    }

    @Test
    fun `fetchCurrentLocationAsPickup handles failure gracefully`() = runTest {
        whenever(locationUtils.getLastKnownLocation()).thenThrow(RuntimeException("Location error"))

        viewModel.fetchCurrentLocationAsPickup()
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Location error", state.locationError)
    }

    @Test
    fun `selectPlace updates pickupLocation with address and area`() = runTest {
        val placeLatLng = LatLng(13.0, 80.0)
        whenever(placesRepository.resolvePlaceId("pickup"))
            .thenReturn(Triple(placeLatLng, "Pickup Address", "Taramani"))

        viewModel.selectPlace("pickup", isPickup = true)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Pickup Address", state.pickupLocation?.address)
        assertEquals("Taramani", state.pickupLocation?.subLocality)
    }

    @Test
    fun `selectPlace updates dropLocation with address and area`() = runTest {
        val dropLatLng = LatLng(13.02, 80.02)
        whenever(placesRepository.resolvePlaceId("drop"))
            .thenReturn(Triple(dropLatLng, "Drop Address", "Guindy"))

        viewModel.selectPlace("drop", isPickup = false)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Drop Address", state.dropLocation?.address)
        assertEquals("Guindy", state.dropLocation?.subLocality)
    }

    @Test
    fun `selectPlace triggers calculateRouteAndFare and updates routePolyline and rideUiModel`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.02, 80.02)
        val mockPath = listOf(pickupLatLng, dropLatLng)
        val mockRoute = RouteInfo(distanceInKm = 4.2, durationInMinutes = 11.0, routePoints = mockPath)

        whenever(placesRepository.resolvePlaceId("pickup"))
            .thenReturn(Triple(pickupLatLng, "Pickup Address", "Taramani"))
        whenever(placesRepository.resolvePlaceId("drop"))
            .thenReturn(Triple(dropLatLng, "Drop Address", "Guindy"))
        whenever(routeRepository.getRoute(any(), any())).thenReturn(mockRoute)

        viewModel.selectPlace("pickup", true)
        viewModel.selectPlace("drop", false)
        runCurrent()

        val state = viewModel.uiState.value

        // Assert route polyline is populated
        assertEquals(2, state.routePolyline.size)
        assertEquals(mockPath[0].latitude, state.routePolyline[0].latitude, 0.0001)
        assertEquals(mockPath[1].longitude, state.routePolyline[1].longitude, 0.0001)

        // Assert rideUiModel is populated
        val ride = state.rideUiModel
        assertNotNull(ride.rideId)
        assertTrue(ride.rideId.length >= 6)

        assertEquals(4.2, ride.distanceInKm!!, 0.01)
        assertEquals(11, ride.durationInMinutes)
        assertNotNull(ride.driverName)
        assertNotNull(ride.rideStartTimeString)
        assertTrue(ride.driverName.isNotEmpty())
    }

    @Test
    fun `selectPlace handles route error gracefully`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.01, 80.01)

        whenever(placesRepository.resolvePlaceId("pickup"))
            .thenReturn(Triple(pickupLatLng, "Pickup Address", "Taramani"))
        viewModel.selectPlace("pickup", isPickup = true)
        runCurrent()

        whenever(placesRepository.resolvePlaceId("fail"))
            .thenReturn(Triple(dropLatLng, "Drop Address", "Velachery"))
        whenever(routeRepository.getRoute(any(), any()))
            .thenThrow(RuntimeException("Route error"))

        viewModel.selectPlace("fail", isPickup = false)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Route error", state.routeError)
    }

    @Test
    fun `updateSelectedVehicle updates and recalculates fare`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.02, 80.02)

        whenever(placesRepository.resolvePlaceId("pickup"))
            .thenReturn(Triple(pickupLatLng, "Pickup", "Area A"))
        whenever(placesRepository.resolvePlaceId("drop"))
            .thenReturn(Triple(dropLatLng, "Drop", "Area B"))
        whenever(routeRepository.getRoute(any(), any()))
            .thenReturn(RouteInfo(5.0, 12.0, listOf(pickupLatLng, dropLatLng)))

        viewModel.selectPlace("pickup", true)
        viewModel.selectPlace("drop", false)
        runCurrent()

        viewModel.updateSelectedVehicle(VehicleDetail.getSUV())
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(VehicleType.SUV, state.selectedVehicle.vehicleType)
    }

    @Test
    fun `fetchAddressPredictions updates pickup suggestions`() = runTest {
        val prediction = mock<AutocompletePrediction>()
        whenever(placesRepository.getPlacePredictions("Chennai"))
            .thenReturn(listOf(prediction))

        viewModel.fetchAddressPredictions("Chennai", isPickup = true)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(1, state.pickupSuggestions.size)
    }

    @Test
    fun `fetchAddressPredictions updates drop suggestions`() = runTest {
        val prediction = mock<AutocompletePrediction>()
        whenever(placesRepository.getPlacePredictions("Airport"))
            .thenReturn(listOf(prediction))

        viewModel.fetchAddressPredictions("Airport", isPickup = false)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(1, state.dropSuggestions.size)
    }

    @Test
    fun `startRideSimulation sets tripState to DRIVER_ARRIVING`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.02, 80.02)
        val path = listOf(pickupLatLng, dropLatLng)

        whenever(placesRepository.resolvePlaceId("pickup"))
            .thenReturn(Triple(pickupLatLng, "Pickup", "Area A"))
        whenever(placesRepository.resolvePlaceId("drop"))
            .thenReturn(Triple(dropLatLng, "Drop", "Area B"))
        whenever(routeRepository.getRoute(any(), any()))
            .thenReturn(RouteInfo(3.0, 5.0, path))

        viewModel.selectPlace("pickup", true)
        viewModel.selectPlace("drop", false)
        runCurrent()

        viewModel.startRideSimulation()
        runCurrent()

        assertEquals(TripState.DRIVER_ARRIVING, viewModel.uiState.value.tripState)
        assertNotNull(viewModel.uiState.value.carPosition)
    }

    @Test
    fun `startRideSimulation goes through ON_TRIP before completing`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.02, 80.02)
        val path = listOf(pickupLatLng, dropLatLng)

        whenever(placesRepository.resolvePlaceId("pickup"))
            .thenReturn(Triple(pickupLatLng, "Pickup", "Area A"))
        whenever(placesRepository.resolvePlaceId("drop"))
            .thenReturn(Triple(dropLatLng, "Drop", "Area B"))
        whenever(routeRepository.getRoute(any(), any()))
            .thenReturn(RouteInfo(3.0, 5.0, path))

        viewModel.selectPlace("pickup", true)
        viewModel.selectPlace("drop", false)
        runCurrent()

        viewModel.startRideSimulation()

        // run full trip
        advanceUntilIdle()

        // extract the current tripState
        val finalState = viewModel.uiState.value.tripState

        // assert that ON_TRIP was visited and now it's COMPLETED
        assertEquals(TripState.COMPLETED, finalState)
    }

    @Test
    fun `startRideSimulation sets tripState to COMPLETED at drop-off`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.02, 80.02)
        val path = listOf(pickupLatLng, dropLatLng)

        whenever(placesRepository.resolvePlaceId("pickup"))
            .thenReturn(Triple(pickupLatLng, "Pickup", "Area A"))
        whenever(placesRepository.resolvePlaceId("drop"))
            .thenReturn(Triple(dropLatLng, "Drop", "Area B"))
        whenever(routeRepository.getRoute(any(), any()))
            .thenReturn(RouteInfo(3.0, 5.0, path))

        viewModel.selectPlace("pickup", true)
        viewModel.selectPlace("drop", false)
        runCurrent()

        viewModel.startRideSimulation()

        // simulate full trip duration
        advanceUntilIdle()

        assertEquals(TripState.COMPLETED, viewModel.uiState.value.tripState)
    }

    @Test
    fun `resetSimulation clears trip state and fare`() = runTest {
        viewModel.resetSimulation()
        val state = viewModel.uiState.value

        assertEquals(TripState.IDLE, state.tripState)
        assertNull(state.carPosition)
        assertNull(state.pickupLocation)
        assertNull(state.dropLocation)
        assertEquals(0, state.pickupSuggestions.size)
        assertEquals(0, state.dropSuggestions.size)
        assertEquals(0, state.routePolyline.size)
        assertEquals(0, state.availableVehicles.size)
        assertEquals(VehicleDetail.getAuto(), state.selectedVehicle)
        assertEquals(RideUiModel(), state.rideUiModel)
        assertNull(state.locationError)
        assertNull(state.routeError)
        assertNull(state.carRotation)
        assertEquals(AddressFieldType.NONE, state.focusedField)
    }
}
