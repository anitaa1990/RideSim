package com.an.ridesim.data

import com.an.ridesim.model.LatLngPoint
import com.google.gson.annotations.SerializedName

data class RouteApiResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>,
    val overviewPolyline: Polyline,
    val summary: String
)

data class Leg(
    val distance: ValueText,
    val duration: ValueText,
    @SerializedName("end_address")
    val endAddress: String,
    @SerializedName("start_address")
    val startAddress: String,
    @SerializedName("end_location")
    val endLocation: LatLngPoint,
    @SerializedName("start_location")
    val startLocation: LatLngPoint
)

data class ValueText(
    val text: String,
    val value: Double
)

data class Polyline(
    val points: String
)