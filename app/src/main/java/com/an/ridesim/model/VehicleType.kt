package com.an.ridesim.model

import com.an.ridesim.R

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
    val price: Double = 0.0
) {
    companion object {
        fun getAuto() = VehicleDetail(
            vehicleType = VehicleType.AUTO,
            iconResId = R.drawable.ic_auto,
            displayNameId = R.string.vehicle_auto_name,
            descriptionId = R.string.vehicle_auto_desc,
            peopleCount = R.string.vehicle_auto_count
        )

        fun getMini() = VehicleDetail(
            vehicleType = VehicleType.AC_MINI,
            iconResId = R.drawable.ic_mini,
            displayNameId = R.string.vehicle_mini_name,
            descriptionId = R.string.vehicle_mini_desc,
            peopleCount = R.string.vehicle_mini_count
        )

        fun getSedan() = VehicleDetail(
            vehicleType = VehicleType.SEDAN,
            iconResId = R.drawable.ic_sedan,
            displayNameId = R.string.vehicle_sedan_name,
            descriptionId = R.string.vehicle_sedan_desc,
            peopleCount = R.string.vehicle_sedan_count
        )

        fun getSUV() = VehicleDetail(
            vehicleType = VehicleType.SUV,
            iconResId = R.drawable.ic_suv,
            displayNameId = R.string.vehicle_suv_name,
            descriptionId = R.string.vehicle_suv_desc,
            peopleCount = R.string.vehicle_suv_count
        )

        fun getSUVPlus() = VehicleDetail(
            vehicleType = VehicleType.SUV_PLUS,
            iconResId = R.drawable.ic_suv_plus,
            displayNameId = R.string.vehicle_suv_plus_name,
            descriptionId = R.string.vehicle_suv_plus_desc,
            peopleCount = R.string.vehicle_suv_plus_count
        )
    }
}


