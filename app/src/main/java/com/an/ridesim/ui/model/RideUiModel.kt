package com.an.ridesim.ui.model

data class RideUiModel(
    val rideId: String = "",
    val driverName: String = "",
    val distanceInKm: Double? = null,
    val durationInMinutes: Int? = null,
    val rideStartTimeString: String = "",
)