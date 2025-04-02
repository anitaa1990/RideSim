package com.an.ridesim.model

/**
 * Represents the different stages of a simulated ride.
 * This is used by the ViewModel and UI to update what’s shown.
 */
enum class TripState {
    /** Initial state when the user is yet to request a ride */
    IDLE,

    /** After the user presses "Book Ride", the driver is approaching the pickup point */
    DRIVER_ARRIVING,

    /** Once the driver reaches the pickup point and the trip has begun */
    ON_TRIP,

    /** When the simulated ride ends */
    COMPLETED
}

fun TripState.peekHeight() = when (this) {
    TripState.IDLE  -> 0.5f
    TripState.DRIVER_ARRIVING, TripState.ON_TRIP, TripState.COMPLETED -> 0.4f
}
