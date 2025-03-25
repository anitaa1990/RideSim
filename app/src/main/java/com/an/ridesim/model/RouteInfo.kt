package com.an.ridesim.model

import com.google.android.gms.maps.model.LatLng

data class RouteInfo(
    val distanceInKm: Double,
    val durationInMinutes: Double,
    val routePoints: List<LatLng> // Polyline coordinates
)