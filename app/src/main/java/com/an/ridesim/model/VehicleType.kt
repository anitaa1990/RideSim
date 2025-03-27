package com.an.ridesim.model

enum class VehicleType {
    AUTO,
    AC_MINI,
    SEDAN,
    SUV,
    SUV_PLUS
}

data class VehicleDetail(
    val vehicleType: VehicleType,
    val iconResId: Int,
    val displayNameId: Int,
    val descriptionId: Int,
    val peopleCount: Int,
    val price: Double
)


