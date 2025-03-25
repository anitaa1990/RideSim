package com.an.ridesim.util

import com.an.ridesim.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Test

class FareCalculatorTest {

    @Test
    fun testAutoFare() {
        // distance = 1.8 (within base), no wait → 35 + 20
        assertEquals(55, FareCalculator.calculateFare(1.8, 0, VehicleType.AUTO))

        // distance = 6.5 → 35 + (4.5 * 16) + 20 + (2 * 1.5 wait)
        // 35 + 72 + 20 + 3 = 130
        assertEquals(130, FareCalculator.calculateFare(6.5, 5, VehicleType.AUTO))
    }

    @Test
    fun testAcMiniFare() {
        // distance = 3.5 → base fare (110) + 20
        assertEquals(130, FareCalculator.calculateFare(3.5, 0, VehicleType.AC_MINI))

        // distance = 7 → 110 + (3 * 19) + 20 + (1 * 1.5 wait)
        // 110 + 57 + 20 + 1.5 = 188.5 ≈ 189
        assertEquals(189, FareCalculator.calculateFare(7.0, 4, VehicleType.AC_MINI))

        // distance = 12 → 110 + (6 * 19) + (2 * 17) + 20 + (4 * 1.5)
        // = 110 + 114 + 34 + 20 + 6 = 284
        assertEquals(284, FareCalculator.calculateFare(12.0, 7, VehicleType.AC_MINI))
    }

    @Test
    fun testSedanFare() {
        // distance = 3 → base fare (130) + 20 + wait (7 * 1.5)
        // 130 + 20 + 10.5 = 160.5 ≈ 161
        assertEquals(161, FareCalculator.calculateFare(3.0, 10, VehicleType.SEDAN))

        // distance = 8 → 130 + (4 * 22) + 20
        // = 130 + 88 + 20 = 238
        assertEquals(238, FareCalculator.calculateFare(8.0, 2, VehicleType.SEDAN))

        // distance = 12 → 130 + (6 * 22) + (2 * 19) + 20 + (3 * 1.5)
        // = 130 + 132 + 38 + 20 + 4.5 = 324.5 ≈ 325
        assertEquals(325, FareCalculator.calculateFare(12.0, 6, VehicleType.SEDAN))
    }

    @Test
    fun testSuvFare() {
        // distance = 4 → base fare (200) + 20
        assertEquals(220, FareCalculator.calculateFare(4.0, 3, VehicleType.SUV))

        // distance = 15 → 200 + (11 * 30) + 20 + (3 * 1.5)
        // = 200 + 330 + 20 + 4.5 = 554.5 ≈ 555
        assertEquals(555, FareCalculator.calculateFare(15.0, 6, VehicleType.SUV))

        // distance = 25 → 200 + (16 * 30) + (5 * 26) + 20 + (6 * 1.5)
        // = 200 + 480 + 130 + 20 + 9 = 839
        assertEquals(839, FareCalculator.calculateFare(25.0, 9, VehicleType.SUV))
    }

    @Test
    fun testSuvPlusFare() {
        // distance = 4 → base fare (300) + 50
        assertEquals(350, FareCalculator.calculateFare(4.0, 0, VehicleType.SUV_PLUS))

        // distance = 10 → 300 + (6 * 38) + 50 + (2 * 1.5)
        // = 300 + 228 + 50 + 3 = 581
        assertEquals(581, FareCalculator.calculateFare(10.0, 5, VehicleType.SUV_PLUS))

        // distance = 20 → 300 + (16 * 38) + 50 + (5 * 1.5)
        // = 300 + 608 + 50 + 7.5 = 965.5 ≈ 966
        assertEquals(966, FareCalculator.calculateFare(20.0, 8, VehicleType.SUV_PLUS))
    }

    @Test
    fun testZeroDistanceCases() {
        // Auto: 0.0 km → base fare (35) + pickup (20)
        assertEquals(55, FareCalculator.calculateFare(0.0, 0, VehicleType.AUTO))
    }

    @Test
    fun testZeroDistanceWithWait() {
        // Sedan: 0 km + 5 min → base (130) + pickup (20) + 2*1.5 = 153
        assertEquals(153, FareCalculator.calculateFare(0.0, 5, VehicleType.SEDAN))
    }
}
