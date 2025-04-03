package com.an.ridesim.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.an.ridesim.model.LatLngPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resumeWithException
import kotlin.math.*
import kotlin.random.Random

/**
 * A utility class that provides geographical calculations
 * such as distance, estimated duration, bearing, and nearby points.
 *
 * - Calculating trip distance between pickup and drop
 * - Estimating ride duration
 * - Simulating a driver's origin by generating a nearby point
 * - Calculating bearing for rotating car markers on the map
 */
@Suppress("DEPRECATION")
class LocationUtils @Inject constructor(
    private val context: Context
) {
    fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * Fetches the user's last known location using the FusedLocationProviderClient.
     * Requires location permission to be granted.
     *
     * @param context The application context.
     * @return Location object with latitude and longitude, or null if unavailable.
     */
    @SuppressLint("MissingPermission") // Permission should be handled before calling this method
    suspend fun getLastKnownLocation(): LatLngPoint? {
        return suspendCancellableCoroutine { cont ->
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    cont.resume(LatLngPoint(location.latitude, location.longitude)) { _, _, _ -> }
                }
                .addOnFailureListener { exception ->
                    cont.resumeWithException(exception)
                }
        }
    }

    suspend fun getAddressFromLatLng(latLng: LatLngPoint): Pair<String?, String?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                Geocoder(context, Locale.getDefault()).getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1,
                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val address = addresses.firstOrNull()?.getAddressLine(0)
                            val area = addresses.firstOrNull()?.subLocality ?: addresses.firstOrNull()?.locality
                            cont.resume(Pair(address, area)) { _, _, _ -> }
                        }

                        override fun onError(errorMessage: String?) {
                            cont.resume(Pair(null, null)) { _, _, _ -> }
                        }
                    }
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val addresses = Geocoder(context, Locale.getDefault())
                        .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    val address = addresses?.firstOrNull()?.getAddressLine(0)
                    val area = addresses?.firstOrNull()?.subLocality ?: addresses?.firstOrNull()?.locality
                    Pair(address, area)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Pair(null, null)
                }
            }
        }
    }


    private val EARTH_RADIUS_KM = 6371.0  // Average radius of the Earth
    private val AVERAGE_SPEED_KMH = 40.0  // Used for estimating trip duration

    /**
     * Calculates the shortest distance between two points on Earth
     * using the Haversine formula.
     *
     * Haversine accounts for Earth's curvature.
     *
     * @param start starting coordinate (latitude, longitude)
     * @param end ending coordinate
     * @return distance in kilometers (Double)
     */
    fun calculateHaversineDistance(start: LatLngPoint, end: LatLngPoint): Double {
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)

        val a = sin(dLat / 2).pow(2.0) +
                sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Estimates trip duration (in minutes) based on a simple average speed.
     *
     * @param distanceKm the distance between pickup and drop
     * @return duration in minutes (Int)
     */
    fun estimateTravelTime(distanceKm: Double): Int {
        return (distanceKm / AVERAGE_SPEED_KMH * 60).roundToInt()
    }

    /**
     * Generates a point N kilometers away from the origin, in a random direction.
     * Used to simulate a driver starting from a nearby location (e.g., 1.1km away).
     *
     * @param origin user's pickup location
     * @param distanceInKm how far the simulated driver starts from
     * @return a new coordinate approximately `distanceInKm` away from origin
     */
    fun generateNearbyPoint(origin: LatLngPoint, distanceInKm: Double): LatLngPoint {
        val bearing = Random.nextDouble(0.0, 2 * Math.PI) // random angle
        val angularDistance = distanceInKm / EARTH_RADIUS_KM

        val lat1 = Math.toRadians(origin.latitude)
        val lon1 = Math.toRadians(origin.longitude)

        val lat2 = asin(
            sin(lat1) * cos(angularDistance) +
                    cos(lat1) * sin(angularDistance) * cos(bearing)
        )

        val lon2 = lon1 + atan2(
            sin(bearing) * sin(angularDistance) * cos(lat1),
            cos(angularDistance) - sin(lat1) * sin(lat2)
        )

        return LatLngPoint(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    /**
     * Calculates the initial bearing (direction) in degrees between two coordinates.
     * Useful for rotating the car marker on the map to simulate real-world direction.
     *
     * @return bearing in degrees (0°–360°)
     */
    fun calculateBearing(start: LatLngPoint, end: LatLngPoint): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(dLon)

        val bearingRad = atan2(y, x)
        return (Math.toDegrees(bearingRad) + 360).toFloat() % 360
    }

    /**
     * Formats a distance to a readable string for UI display.
     * - 0.0–1.0km → shows 1 km
     * - Otherwise rounds to 1 decimal place
     */
    fun formatDistanceKm(distanceKm: Double): String {
        return if (distanceKm < 1.0) "1 km"
        else "${"%.1f".format(distanceKm)} km"
    }
}
