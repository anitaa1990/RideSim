package com.an.ridesim.ui.model

data class RideStatusUiModel(
    val hasDriverArrived: Boolean = false,
    val distanceToTarget: Double? = null
)
