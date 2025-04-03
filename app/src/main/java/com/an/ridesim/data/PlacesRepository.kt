package com.an.ridesim.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

/**
 * [PlacesRepository] provides access to Google's Places SDK
 * to fetch location search suggestions and resolve place details.
 * It wraps the async SDK calls in suspend functions for easy use
 * in ViewModels and Jetpack Compose UIs.
 */
class PlacesRepository @Inject constructor(
    private val placesClient: PlacesClient
) {
    /**
     * Returns a list of autocomplete suggestions based on user input.
     *
     * @param query The partial location text typed by the user.
     * @return List of [AutocompletePrediction] objects matching the query.
     */
    suspend fun getPlacePredictions(query: String): List<AutocompletePrediction> {
        return suspendCancellableCoroutine { cont ->
            // Build request with the query string
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            // Call the SDK to fetch predictions
            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    // Resume coroutine with result when successful
                    cont.resume(response.autocompletePredictions) { _, _, _ -> }
                }
                .addOnFailureListener { exception ->
                    // Resume with exception so caller can handle it
                    cont.resumeWithException(exception)
                }
        }
    }

    /**
     * Given a place ID (from a prediction), returns the [LatLng] and address.
     *
     * @param placeId The unique Google Place ID.
     * @return A [Pair] containing the LatLng and full address, or null if not found.
     */
    suspend fun resolvePlaceId(placeId: String): Triple<LatLng, String, String?>? {
        return suspendCancellableCoroutine { cont ->
            // Request the required fields (lat/lng and address)
            val placeFields = listOf(
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS
            )
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()

            // Call the SDK to fetch the place details
            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val latLng = response.place.location
                    val address = response.place.formattedAddress
                    val components = response.place.addressComponents?.asList()
                    val area = components
                        ?.firstOrNull {
                            it.types.contains("sublocality") || it.types.contains("locality")
                        }
                        ?.name

                    if (latLng != null && address != null) {
                        // Resume with location and address if both are valid
                        cont.resume(Triple(latLng, address, area)) { _, _, _ -> }
                    } else {
                        // Resume with null if incomplete result
                        cont.resume(null) { _, _, _ -> }
                    }
                }
                .addOnFailureListener { exception ->
                    cont.resumeWithException(exception)
                }
        }
    }
}
