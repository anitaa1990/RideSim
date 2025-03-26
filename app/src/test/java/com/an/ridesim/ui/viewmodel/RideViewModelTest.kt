package com.an.ridesim.ui.viewmodel

import android.location.Location
import com.an.ridesim.data.PlacesRepository
import com.an.ridesim.data.RouteRepository
import com.an.ridesim.model.*
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

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RideViewModel(placesRepository, routeRepository, locationUtils)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchCurrentLocationAsPickup updates pickupLocation and address`() = runTest {
        val location = Location("mock").apply {
            latitude = 13.0827
            longitude = 80.2707
        }
        whenever(locationUtils.getLastKnownLocation()).thenReturn(location)

        viewModel.fetchCurrentLocationAsPickup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(13.0827, state.pickupLocation?.latitude!!, 0.001)
        assertEquals(80.2707, state.pickupLocation.longitude, 0.001)
        assertEquals("Current Location", state.pickupAddress)
    }

    @Test
    fun `fetchCurrentLocationAsPickup handles failure gracefully`() = runTest {
        whenever(locationUtils.getLastKnownLocation()).thenThrow(RuntimeException("Location error"))

        viewModel.fetchCurrentLocationAsPickup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Location error", state.locationError)
    }

    @Test
    fun `selectPlace updates pickup location`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.01, 80.01)

        // Setup drop location so route gets triggered
        whenever(placesRepository.resolvePlaceId("drop")).thenReturn(Pair(dropLatLng, "Drop"))
        viewModel.selectPlace("drop", isPickup = false)
        advanceUntilIdle()

        whenever(placesRepository.resolvePlaceId("pickup")).thenReturn(Pair(pickupLatLng, "Pickup"))
        whenever(routeRepository.getRoute(any(), any())).thenReturn(
            RouteInfo(4.0, 10.0, listOf(pickupLatLng))
        )

        viewModel.selectPlace("pickup", isPickup = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Pickup", state.pickupAddress)
        assertEquals(4.0, state.distanceInKm!!, 0.01)
        assertEquals(10, state.durationInMinutes)
    }

    @Test
    fun `selectPlace updates drop location`() = runTest {
        val pickupLatLng = LatLng(13.01, 80.01)
        val dropLatLng = LatLng(13.02, 80.02)

        // ✅ Set pickup first so both locations are available
        whenever(placesRepository.resolvePlaceId("pickup")).thenReturn(Pair(pickupLatLng, "Pickup"))
        viewModel.selectPlace("pickup", isPickup = true)
        advanceUntilIdle()

        // ✅ Then test drop selection
        whenever(placesRepository.resolvePlaceId("drop")).thenReturn(Pair(dropLatLng, "Drop"))
        whenever(routeRepository.getRoute(any(), any())).thenReturn(
            RouteInfo(6.0, 14.0, listOf(dropLatLng))
        )

        viewModel.selectPlace("drop", isPickup = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Drop", state.dropAddress)
        assertEquals(6.0, state.distanceInKm!!, 0.01)
        assertEquals(14, state.durationInMinutes)
    }

    @Test
    fun `selectPlace handles route error gracefully`() = runTest {
        val pickupLatLng = LatLng(13.0, 80.0)
        val dropLatLng = LatLng(13.01, 80.01)

        // Setup pickup first to satisfy the condition
        whenever(placesRepository.resolvePlaceId("pickup")).thenReturn(Pair(pickupLatLng, "Pickup"))
        viewModel.selectPlace("pickup", isPickup = true)
        advanceUntilIdle()

        // Setup routeRepository to throw
        whenever(placesRepository.resolvePlaceId("fail")).thenReturn(Pair(dropLatLng, "Fail Place"))
        whenever(routeRepository.getRoute(any(), any()))
            .thenThrow(RuntimeException("Route error"))

        viewModel.selectPlace("fail", isPickup = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Route error", state.routeError)
    }

    @Test
    fun `updateVehicleType updates selected vehicle and fare`() = runTest {
        val latLng = LatLng(13.0, 80.0)
        whenever(placesRepository.resolvePlaceId(any())).thenReturn(Pair(latLng, "Address"))
        whenever(routeRepository.getRoute(any(), any())).thenReturn(
            RouteInfo(5.0, 12.0, listOf(latLng))
        )

        viewModel.selectPlace("pickup", isPickup = true)
        viewModel.selectPlace("drop", isPickup = false)
        advanceUntilIdle()

        viewModel.updateVehicleType(VehicleType.SEDAN)
        val state = viewModel.uiState.value

        assertEquals(VehicleType.SEDAN, state.selectedVehicle)
        assertNotNull(state.estimatedFare)
    }

    @Test
    fun `fetchAddressPredictions updates pickup suggestions`() = runTest {
        val prediction = mock<AutocompletePrediction>()
        whenever(placesRepository.getPlacePredictions("Chennai"))
            .thenReturn(listOf(prediction))

        viewModel.fetchAddressPredictions("Chennai", isPickup = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.pickupSuggestions.size)
    }

    @Test
    fun `startRideSimulation updates tripState and carPosition then completes`() = runTest {
        val path = listOf(
            LatLng(13.0, 80.0),
            LatLng(13.01, 80.01),
            LatLng(13.02, 80.02)
        )
        val latLng = LatLng(13.0, 80.0)

        whenever(placesRepository.resolvePlaceId(any())).thenReturn(Pair(latLng, "Address"))
        whenever(routeRepository.getRoute(any(), any())).thenReturn(
            RouteInfo(5.0, 10.0, path)
        )

        viewModel.selectPlace("pickup", isPickup = true)
        viewModel.selectPlace("drop", isPickup = false)
        advanceUntilIdle()

        viewModel.startRideSimulation()

        // 1️⃣ Expect DRIVER_ARRIVING first
        assertEquals(TripState.DRIVER_ARRIVING, viewModel.uiState.value.tripState)

        // 2️⃣ Move to ON_TRIP
        advanceTimeBy(3000)
        runCurrent()
        assertEquals(TripState.ON_TRIP, viewModel.uiState.value.tripState)

        // 3️⃣ Simulate car movement
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(TripState.ON_TRIP, viewModel.uiState.value.tripState)
        assertNotNull(viewModel.uiState.value.carPosition)

        // 4️⃣ Move to COMPLETED
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(TripState.COMPLETED, viewModel.uiState.value.tripState)
        assertNull(viewModel.uiState.value.carPosition)
    }

    @Test
    fun `resetSimulation clears trip state and fare`() = runTest {
        viewModel.resetSimulation()
        val state = viewModel.uiState.value

        assertEquals(TripState.IDLE, state.tripState)
        assertNull(state.estimatedFare)
        assertNull(state.carPosition)
    }
}
