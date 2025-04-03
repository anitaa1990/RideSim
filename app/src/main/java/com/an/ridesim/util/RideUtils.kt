package com.an.ridesim.util

import com.an.ridesim.model.VehicleType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RideUtils {
    private val driverNames = listOf(
        "RANGARAJAN R",
        "VIKRAM K",
        "ARUN P",
        "SURESH N",
        "KARTHIK M",
        "RAJESH V",
        "DEEPAK S",
        "SANJAY R",
        "MUKESH T",
        "ARAVIND R",
        "MANOJ L",
        "BALAJI D",
        "NAVEEN K",
        "RAVI G",
        "KUMAR A",
        "MOHAN B",
        "PRAVEEN J",
        "RAMESH C",
        "GANESH V",
        "NARAYANAN M"
    )

    private val autoPlates = listOf(
        "TN 01 AB 1234", "TN 02 CD 5678", "TN 03 EF 9101", "TN 04 GH 1121", "TN 05 IJ 3141",
        "TN 06 KL 5161", "TN 07 MN 7181", "TN 08 OP 9202", "TN 09 QR 1222", "TN 10 ST 3242",
        "TN 11 UV 5262", "TN 12 WX 7282", "TN 13 YZ 9303", "TN 14 AB 1323", "TN 15 CD 3343",
        "TN 16 EF 5363", "TN 17 GH 7383", "TN 18 IJ 9404", "TN 19 KL 1424", "TN 20 MN 3444"
    )

    private val miniPlates = listOf(
        "TN 21 OP 5464", "TN 22 QR 7484", "TN 23 ST 9505", "TN 24 UV 1525", "TN 25 WX 3545",
        "TN 26 YZ 5565", "TN 27 AB 7585", "TN 28 CD 9606", "TN 29 EF 1626", "TN 30 GH 3646",
        "TN 31 IJ 5666", "TN 32 KL 7686", "TN 33 MN 9707", "TN 34 OP 1727", "TN 35 QR 3747",
        "TN 36 ST 5767", "TN 37 UV 7787", "TN 38 WX 9808", "TN 39 YZ 1828", "TN 40 AB 3848"
    )

    private val sedanPlates = listOf(
        "TN 41 CD 5868", "TN 42 EF 7888", "TN 43 GH 9909", "TN 44 IJ 1929", "TN 45 KL 3949",
        "TN 46 MN 5969", "TN 47 OP 7989", "TN 48 QR 9000", "TN 49 ST 2020", "TN 50 UV 4040",
        "TN 51 WX 6060", "TN 52 YZ 8080", "TN 53 AB 0101", "TN 54 CD 2121", "TN 55 EF 4141",
        "TN 56 GH 6161", "TN 57 IJ 8181", "TN 58 KL 0202", "TN 59 MN 2222", "TN 60 OP 4242"
    )

    private val suvPlates = listOf(
        "TN 61 QR 6262", "TN 62 ST 8282", "TN 63 UV 0303", "TN 64 WX 2323", "TN 65 YZ 4343",
        "TN 66 AB 6363", "TN 67 CD 8383", "TN 68 EF 0404", "TN 69 GH 2424", "TN 70 IJ 4444",
        "TN 71 KL 6464", "TN 72 MN 8484", "TN 73 OP 0505", "TN 74 QR 2525", "TN 75 ST 4545",
        "TN 76 UV 6565", "TN 77 WX 8585", "TN 78 YZ 0606", "TN 79 AB 2626", "TN 80 CD 4646"
    )

    private val suvPlusPlates = listOf(
        "TN 81 EF 6666", "TN 82 GH 8686", "TN 83 IJ 0707", "TN 84 KL 2727", "TN 85 MN 4747",
        "TN 86 OP 6767", "TN 87 QR 8787", "TN 88 ST 0808", "TN 89 UV 2828", "TN 90 WX 4848",
        "TN 91 YZ 6868", "TN 92 AB 8888", "TN 93 CD 0909", "TN 94 EF 2929", "TN 95 GH 4949",
        "TN 96 IJ 6969", "TN 97 KL 8989", "TN 98 MN 1010", "TN 99 OP 3030", "TN 99 QR 5050"
    )

    fun getRandomDriverName(): String {
        return driverNames.random()
    }

    fun getRandomPlateForVehicleType(
        vehicleType: VehicleType
    ) = when (vehicleType) {
        VehicleType.AUTO -> autoPlates.random()
        VehicleType.AC_MINI -> miniPlates.random()
        VehicleType.SEDAN -> sedanPlates.random()
        VehicleType.SUV -> suvPlates.random()
        VehicleType.SUV_PLUS -> suvPlusPlates.random()
    }

    fun getCurrentDateTimeFormatted(): String {
        val now = Date()
        val dateFormat = SimpleDateFormat("EEE, MMM d • h:mma", Locale.getDefault())
        return dateFormat.format(now)
    }
}