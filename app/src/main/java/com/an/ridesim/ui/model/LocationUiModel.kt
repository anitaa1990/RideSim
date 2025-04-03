package com.an.ridesim.ui.model

import com.an.ridesim.model.LatLngPoint

data class LocationUiModel(
    val address: String? = null,
    val subLocality: String? = null,
    val locationPoint: LatLngPoint? = null
)