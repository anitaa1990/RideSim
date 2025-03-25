package com.an.ridesim.data

import com.an.ridesim.BuildConfig
import com.an.ridesim.model.RouteInfo
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import javax.inject.Inject

class RouteRepository @Inject constructor(
    private val routeApiService: RouteApiService
) {
    suspend fun getRoute(
        origin: LatLng,
        destination: LatLng
    ): RouteInfo? {
        return try {
            val apiResponse = routeApiService.getRoute(
                origin = "${origin.latitude},${origin.longitude}",
                destination = "${destination.latitude},${destination.longitude}",
                apiKey = BuildConfig.MAPS_API_KEY
            )

            if (apiResponse.isSuccessful) {
                val route = apiResponse.body()?.routes?.firstOrNull()
                val leg = route?.legs?.firstOrNull()
                val encodedPolyline = route?.overviewPolyline?.points

                if (leg != null && !encodedPolyline.isNullOrBlank()) {
                    RouteInfo(
                        distanceInKm = leg.distance.value / 1000.0,
                        durationInMinutes = leg.duration.value / 60.0,
                        routePoints = PolyUtil.decode(encodedPolyline)
                    )
                } else null
            } else null
        } catch (e: Exception) {
            println(e)
            return null
        }
    }
}