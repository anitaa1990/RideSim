package com.an.ridesim.model

import com.an.ridesim.R

enum class VehicleType {
    AUTO,
    AC_MINI,
    SEDAN,
    SUV,
    SUV_PLUS
}

fun VehicleType.getImageId() = when(this) {
    VehicleType.AUTO -> R.drawable.ic_full_auto
    VehicleType.AC_MINI -> R.drawable.ic_full_mini
    VehicleType.SEDAN -> R.drawable.ic_full_sedan
    VehicleType.SUV -> R.drawable.ic_full_suv
    VehicleType.SUV_PLUS -> R.drawable.ic_full_suv_plus
}

fun VehicleType.getIconId() = when(this) {
    VehicleType.AUTO -> R.drawable.ic_front_auto
    else -> R.drawable.ic_front_car
}

data class VehicleDetail(
    val vehicleType: VehicleType,
    val iconResId: Int,
    val markerIconResId: Int,
    val displayNameId: Int,
    val descriptionId: Int,
    val peopleCount: Int,
    val price: Double = 0.0
) {
    companion object {
        fun getAuto() = VehicleDetail(
            vehicleType = VehicleType.AUTO,
            iconResId = R.drawable.ic_auto,
            markerIconResId = R.drawable.ic_marker_auto,
            displayNameId = R.string.vehicle_auto_name,
            descriptionId = R.string.vehicle_auto_desc,
            peopleCount = R.string.vehicle_auto_count
        )

        fun getMini() = VehicleDetail(
            vehicleType = VehicleType.AC_MINI,
            iconResId = R.drawable.ic_mini,
            markerIconResId = R.drawable.ic_marker_mini,
            displayNameId = R.string.vehicle_mini_name,
            descriptionId = R.string.vehicle_mini_desc,
            peopleCount = R.string.vehicle_mini_count
        )

        fun getSedan() = VehicleDetail(
            vehicleType = VehicleType.SEDAN,
            iconResId = R.drawable.ic_sedan,
            markerIconResId = R.drawable.ic_marker_sedan,
            displayNameId = R.string.vehicle_sedan_name,
            descriptionId = R.string.vehicle_sedan_desc,
            peopleCount = R.string.vehicle_sedan_count
        )

        fun getSUV() = VehicleDetail(
            vehicleType = VehicleType.SUV,
            iconResId = R.drawable.ic_suv,
            markerIconResId = R.drawable.ic_marker_suv,
            displayNameId = R.string.vehicle_suv_name,
            descriptionId = R.string.vehicle_suv_desc,
            peopleCount = R.string.vehicle_suv_count
        )

        fun getSUVPlus() = VehicleDetail(
            vehicleType = VehicleType.SUV_PLUS,
            iconResId = R.drawable.ic_suv_plus,
            markerIconResId = R.drawable.ic_marker_suv_plus,
            displayNameId = R.string.vehicle_suv_plus_name,
            descriptionId = R.string.vehicle_suv_plus_desc,
            peopleCount = R.string.vehicle_suv_plus_count
        )
    }
}


