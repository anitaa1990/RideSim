package com.an.ridesim.util

import com.an.ridesim.model.LatLngPoint
import org.junit.Assert.*
import org.junit.Test

class LocationUtilsTest {

    private val chennaiCentral = LatLngPoint(13.0827, 80.2707)
    private val chennaiAirport = LatLngPoint(12.9941, 80.1808)
    private val bangaloreMG = LatLngPoint(12.9716, 77.5946)
    private val delta = 0.1  // Acceptable precision range for km values

    @Test
    fun `distance between Chennai Central and Chennai Airport should be ~14 km`() {
        val distance = LocationUtils.calculateHaversineDistance(chennaiCentral, chennaiAirport)
        assertEquals(14.0, distance, 2.0)
    }

    @Test
    fun `distance between Chennai and Bangalore should be ~292 km`() {
        val distance = LocationUtils.calculateHaversineDistance(chennaiCentral, bangaloreMG)
        assertEquals(292.0, distance, 10.0)
    }

    @Test
    fun `duration estimate for 20 km should be 30 mins`() {
        val time = LocationUtils.estimateTravelTime(20.0)
        assertEquals(30, time)
    }

    @Test
    fun `generateNearbyPoint should return a point approx 1 km away`() {
        val point = LocationUtils.generateNearbyPoint(chennaiCentral, 1.0)
        val distance = LocationUtils.calculateHaversineDistance(chennaiCentral, point)
        assertTrue(distance in 0.8..1.2)
    }

    @Test
    fun `bearing should return a valid value between 0 and 360`() {
        val bearing = LocationUtils.calculateBearing(chennaiCentral, chennaiAirport)
        assertTrue(bearing in 0.0..360.0)
    }

    @Test
    fun `formatDistance should round up correctly`() {
        val label1 = LocationUtils.formatDistanceKm(0.3)
        val label2 = LocationUtils.formatDistanceKm(1.456)
        val label3 = LocationUtils.formatDistanceKm(10.0)

        assertEquals("1 km", label1)
        assertEquals("1.5 km", label2)
        assertEquals("10.0 km", label3)
    }
}
