package com.an.ridesim.model

import com.google.android.gms.maps.model.LatLng

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double
)

fun LatLngPoint.toLatLng() = LatLng(latitude, longitude)
fun LatLng.toLatLngPoint() = LatLngPoint(latitude, longitude)
