package com.an.ridesim.data

import com.an.ridesim.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.Response

class RouteRepositoryTest {

    private val routeApiService: RouteApiService = mock()
    private lateinit var routeRepository: RouteRepository

    private val origin = LatLng(13.0, 80.0)
    private val destination = LatLng(13.1, 80.1)

    @Before
    fun setup() {
        routeRepository = RouteRepository(routeApiService)
    }

    @Test
    fun `returns RouteInfo when API is successful`() = runBlocking {
        val polyline = "encodedPolyline123"
        val decodedPolyline = listOf(
            LatLng(13.0, 80.0),
            LatLng(13.05, 80.05),
            LatLng(13.1, 80.1)
        )

        // Create fake response matching your models
        val fakeResponse = RouteApiResponse(
            routes = listOf(
                Route(
                    legs = listOf(
                        Leg(
                            distance = ValueText("5.0 km", 5000.0),
                            duration = ValueText("10 mins", 600.0),
                            startAddress = "Start Place",
                            endAddress = "End Place",
                            startLocation = LatLngPoint(13.0, 80.0),
                            endLocation = LatLngPoint(13.1, 80.1)
                        )
                    ),
                    overviewPolyline = Polyline(polyline),
                    summary = "Route Summary"
                )
            )
        )

        // Mock PolyUtil.decode() to avoid decoding logic in unit test
        mockStatic(PolyUtil::class.java).use { mockedStatic ->
            mockedStatic.`when`<List<LatLng>> { PolyUtil.decode(polyline) }.thenReturn(decodedPolyline)

            `when`(
                routeApiService.getRoute(anyString(), anyString(), anyString())
            ).thenReturn(Response.success(fakeResponse))

            val result = routeRepository.getRoute(origin, destination)

            assertNotNull(result)
            assertEquals(5.0, result?.distanceInKm ?: -1.0, 0.01)
            assertEquals(10.0, result?.durationInMinutes ?: -1.0, 0.01)
            assertEquals(decodedPolyline, result?.routePoints)
        }
    }

    @Test
    fun `returns null when API response is unsuccessful`() = runBlocking {
        `when`(
            routeApiService.getRoute(anyString(), anyString(), anyString())
        ).thenReturn(
            Response.error<RouteApiResponse>(
                500,
                "Server error".toResponseBody("application/json".toMediaType())
            )
        )

        val result = routeRepository.getRoute(origin, destination)
        assertNull(result)
    }

    @Test
    fun `returns null when API throws exception`() = runBlocking {
        `when`(
            routeApiService.getRoute(anyString(), anyString(), anyString())
        ).thenThrow(RuntimeException("Network error"))

        val result = routeRepository.getRoute(origin, destination)

        assertNull(result)
    }
}
