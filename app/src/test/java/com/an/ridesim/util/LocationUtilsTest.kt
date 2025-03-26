package com.an.ridesim.util

import com.an.ridesim.model.LatLngPoint
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class LocationUtilsTest {
    private val locationUtils = LocationUtils(mock())

    private val chennaiCentral = LatLngPoint(13.0827, 80.2707)
    private val chennaiAirport = LatLngPoint(12.9941, 80.1808)
    private val bangaloreMG = LatLngPoint(12.9716, 77.5946)

    @Test
    fun `distance between Chennai Central and Chennai Airport should be ~14 km`() {
        val distance = locationUtils.calculateHaversineDistance(chennaiCentral, chennaiAirport)
        assertEquals(14.0, distance, 2.0)
    }

    @Test
    fun `distance between Chennai and Bangalore should be ~292 km`() {
        val distance = locationUtils.calculateHaversineDistance(chennaiCentral, bangaloreMG)
        assertEquals(292.0, distance, 10.0)
    }

    @Test
    fun `duration estimate for 20 km should be 30 mins`() {
        val time = locationUtils.estimateTravelTime(20.0)
        assertEquals(30, time)
    }

    @Test
    fun `generateNearbyPoint should return a point approx 1 km away`() {
        val point = locationUtils.generateNearbyPoint(chennaiCentral, 1.0)
        val distance = locationUtils.calculateHaversineDistance(chennaiCentral, point)
        assertTrue(distance in 0.8..1.2)
    }

    @Test
    fun `bearing should return a valid value between 0 and 360`() {
        val bearing = locationUtils.calculateBearing(chennaiCentral, chennaiAirport)
        assertTrue(bearing in 0.0..360.0)
    }

    @Test
    fun `formatDistance should round up correctly`() {
        val label1 = locationUtils.formatDistanceKm(0.3)
        val label2 = locationUtils.formatDistanceKm(1.456)
        val label3 = locationUtils.formatDistanceKm(10.0)

        assertEquals("1 km", label1)
        assertEquals("1.5 km", label2)
        assertEquals("10.0 km", label3)
    }
}
