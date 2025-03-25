package com.an.ridesim.util

import com.an.ridesim.model.VehicleType
import kotlin.math.roundToInt

/**
 * [FareCalculator] is responsible for computing the estimated trip fare based on:
 * - Distance (in km)
 * - Waiting time (optional, after threshold)
 * - Vehicle type selected by the user (Auto, AC Mini, Sedan, etc.)
 *
 * The fare calculation supports:
 * - Tiered rate slabs depending on distance
 * - Pickup charges
 * - Waiting charges (only if waiting exceeds a threshold)
 *
 * This logic is meant to closely reflects real-world pricing systems for ride-hailing apps.
 */
object FareCalculator {

    /**
     * Calculates the total fare based on provided parameters.
     *
     * @param distanceInKm Total distance between pickup and drop in kilometers.
     * @param waitTimeInMinutes Optional wait time (only minutes beyond the threshold are charged).
     * @param vehicleType Type of vehicle selected (Auto, Sedan, etc.).
     *
     * @return The total fare as an integer (rounded to nearest ₹).
     */
    fun calculateFare(
        distanceInKm: Double,
        waitTimeInMinutes: Int = 0,
        vehicleType: VehicleType
    ): Int {
        // Get the appropriate rate configuration for the selected vehicle
        val config = getFareConfig(vehicleType)

        // Calculate base fare based on distance and tier logic
        val fare = when {
            // If total distance is less than or equal to the minimum threshold
            distanceInKm <= config.minFareKm -> config.minFare.toDouble()

            // If distance is within the tier 1 range (after minimum fare)
            distanceInKm <= config.tier2StartKm -> {
                config.minFare +
                        (distanceInKm - config.minFareKm) * config.tier1RatePerKm
            }

            // If distance exceeds tier 1 range (enter tier 2)
            else -> {
                val tier1Distance = config.tier2StartKm - config.minFareKm
                val tier2Distance = distanceInKm - config.tier2StartKm

                config.minFare +
                        (tier1Distance * config.tier1RatePerKm) +
                        (tier2Distance * config.tier2RatePerKm)
            }
        }

        // Add waiting charge if waiting time exceeds 3 minutes
        val waitFare = if (waitTimeInMinutes > 3) {
            (waitTimeInMinutes - 3) * config.waitingChargePerMinute
        } else 0.0

        // Total = base fare + pickup charge + wait fare
        return (fare + config.pickupCharge + waitFare).roundToInt()
    }

    /**
     * Returns a fare configuration for each supported vehicle type.
     * This defines how pricing tiers and surcharges apply for that type.
     */
    private fun getFareConfig(vehicleType: VehicleType): FareConfig {
        return when (vehicleType) {
            VehicleType.AUTO -> FareConfig(
                minFare = 35,
                minFareKm = 2.0,
                tier1RatePerKm = 16.0,
                tier2StartKm = 2.0,
                tier2RatePerKm = 16.0,
                pickupCharge = 20.0,
                waitingChargePerMinute = 1.5
            )

            VehicleType.AC_MINI -> FareConfig(
                minFare = 110,
                minFareKm = 4.0,
                tier1RatePerKm = 19.0,
                tier2StartKm = 10.0,
                tier2RatePerKm = 17.0,
                pickupCharge = 20.0,
                waitingChargePerMinute = 1.5
            )

            VehicleType.SEDAN -> FareConfig(
                minFare = 130,
                minFareKm = 4.0,
                tier1RatePerKm = 22.0,
                tier2StartKm = 10.0,
                tier2RatePerKm = 19.0,
                pickupCharge = 20.0,
                waitingChargePerMinute = 1.5
            )

            VehicleType.SUV -> FareConfig(
                minFare = 200,
                minFareKm = 4.0,
                tier1RatePerKm = 30.0,
                tier2StartKm = 20.0,
                tier2RatePerKm = 26.0,
                pickupCharge = 20.0,
                waitingChargePerMinute = 1.5
            )

            VehicleType.SUV_PLUS -> FareConfig(
                minFare = 300,
                minFareKm = 4.0,
                tier1RatePerKm = 38.0,
                tier2StartKm = 4.0, // No tier2 — flat rate after 4km
                tier2RatePerKm = 38.0,
                pickupCharge = 50.0,
                waitingChargePerMinute = 1.5
            )
        }
    }

    /**
     * Internal model class representing rate card details for a vehicle.
     *
     * @property minFare Flat fare for trips up to [minFareKm].
     * @property minFareKm Threshold (in km) for applying minFare.
     * @property tier1RatePerKm Price per km for mid-range distances.
     * @property tier2StartKm At what point tier2 pricing kicks in.
     * @property tier2RatePerKm Price per km beyond tier2StartKm.
     * @property pickupCharge Fixed pickup charge added to all fares.
     * @property waitingChargePerMinute Cost per minute after 3 mins of wait.
     */
    private data class FareConfig(
        val minFare: Int,
        val minFareKm: Double,
        val tier1RatePerKm: Double,
        val tier2StartKm: Double,
        val tier2RatePerKm: Double,
        val pickupCharge: Double,
        val waitingChargePerMinute: Double
    )
}
