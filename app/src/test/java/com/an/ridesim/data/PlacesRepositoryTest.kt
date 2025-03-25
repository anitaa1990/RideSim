package com.an.ridesim.data

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesRepositoryTest {

    private lateinit var placesClient: PlacesClient
    private lateinit var repository: PlacesRepository

    @Before
    fun setUp() {
        placesClient = mock()
        repository = PlacesRepository(placesClient)
    }

    @Test
    fun `getPlacePredictions returns predictions on success`() = runTest {
        // Mock Task and Response
        val mockTask = mock<Task<FindAutocompletePredictionsResponse>>()
        val mockResponse = mock<FindAutocompletePredictionsResponse>()
        val predictions = listOf(mock<AutocompletePrediction>())

        // Mock behavior of Task
        whenever(mockResponse.autocompletePredictions).thenReturn(predictions)
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<FindAutocompletePredictionsResponse>>(0)
            listener.onSuccess(mockResponse)
            mockTask
        }
        whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
        whenever(placesClient.findAutocompletePredictions(any())).thenReturn(mockTask)

        // Call the function and verify result
        val result = repository.getPlacePredictions("query")
        assertEquals(predictions, result)
    }

    @Test
    fun `getPlacePredictions throws exception on failure`() = runTest {
        // Mock Task
        val mockTask = mock<Task<FindAutocompletePredictionsResponse>>()
        val exception = RuntimeException("Error fetching predictions")

        // Mock behavior of Task
        whenever(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        whenever(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnFailureListener>(0)
            listener.onFailure(exception)
            mockTask
        }
        whenever(placesClient.findAutocompletePredictions(any())).thenReturn(mockTask)

        // Call the function and verify exception is thrown
        val thrownException = assertFailsWith<RuntimeException> {
            repository.getPlacePredictions("query")
        }
        assertEquals("Error fetching predictions", thrownException.message)
    }

    @Test
    fun `resolvePlaceId returns LatLng and address on success`() = runTest {
        // Mock Task and Response
        val mockTask = mock<Task<FetchPlaceResponse>>()
        val mockResponse = mock<FetchPlaceResponse>()
        val place = mock<Place>()
        val latLng = mock<com.google.android.gms.maps.model.LatLng>()

        // Mock behavior of Place and Response
        whenever(place.location).thenReturn(latLng)
        whenever(place.formattedAddress).thenReturn("123 Main St")
        whenever(mockResponse.place).thenReturn(place)

        // Mock behavior of Task
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<FetchPlaceResponse>>(0)
            listener.onSuccess(mockResponse)
            mockTask
        }
        whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)
        whenever(placesClient.fetchPlace(any())).thenReturn(mockTask)

        // Call the function and verify result
        val result = repository.resolvePlaceId("placeId")
        assertEquals(Pair(latLng, "123 Main St"), result)
    }

    @Test
    fun `resolvePlaceId throws exception on failure`() = runTest {
        // Mock failure from PlacesClient
        val mockTask = mock<Task<FetchPlaceResponse>>()
        val exception = RuntimeException("Error fetching place")

        `when`(mockTask.isSuccessful).thenReturn(false)
        `when`(mockTask.exception).thenReturn(exception)
        `when`(placesClient.fetchPlace(any())).thenReturn(mockTask)

        // Call the function and verify exception is thrown
        assertFailsWith<RuntimeException> { repository.resolvePlaceId("placeId") }
    }
}
