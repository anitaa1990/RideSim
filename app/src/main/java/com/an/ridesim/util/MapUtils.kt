package com.an.ridesim.util

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.an.ridesim.model.LatLngPoint
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object MapUtils {
    /**
     * Linearly interpolates (lerps) between two colors based on a given [fraction].
     * Used to create smooth gradients between a start and end color.
     *
     * @param startColor The ARGB color to start from (Int representation).
     * @param endColor The ARGB color to interpolate to (Int representation).
     * @param fraction A value between 0.0 and 1.0 representing interpolation progress.
     *                 0.0 = startColor, 1.0 = endColor, 0.5 = halfway blend.
     *
     * @return The interpolated color as a packed ARGB Int.
     */
    fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
        // Decompose startColor into alpha, red, green, blue (8-bit components)
        val startA = (startColor shr 24) and 0xff // alpha
        val startR = (startColor shr 16) and 0xff // red
        val startG = (startColor shr 8) and 0xff  // green
        val startB = startColor and 0xff          // blue

        // Decompose endColor into alpha, red, green, blue (8-bit components)
        val endA = (endColor shr 24) and 0xff
        val endR = (endColor shr 16) and 0xff
        val endG = (endColor shr 8) and 0xff
        val endB = endColor and 0xff

        // Linearly interpolate each channel using the formula:
        // result = start + ((end - start) * fraction)
        val a = (startA + ((endA - startA) * fraction)).toInt()
        val r = (startR + ((endR - startR) * fraction)).toInt()
        val g = (startG + ((endG - startG) * fraction)).toInt()
        val b = (startB + ((endB - startB) * fraction)).toInt()

        // Recombine the channels into a single ARGB Int:
        // (alpha << 24) | (red << 16) | (green << 8) | blue
        return (a and 0xff shl 24) or
                (r and 0xff shl 16) or
                (g and 0xff shl 8) or
                (b and 0xff)
    }

    /**
     * Resizes an icon (image) while maintaining its aspect ratio.
     *
     * @param iconResId The resource ID of the icon (drawable) to be resized.
     * @param context The context from which to retrieve the drawable resource.
     * @param width The desired width for the resized icon.
     *
     * @return A [BitmapDescriptor] representing the resized icon, suitable for use as a marker on
     * Google Maps.
     *
     */
    fun getResizedIconWithAspectRatio(
        iconResId: Int,
        context: Context,
        width: Int
    ): BitmapDescriptor {
        // Retrieves the drawable resource from `iconResId`
        val drawable = ContextCompat.getDrawable(context, iconResId)

        // Convert the drawable to bitmap
        val bitmap = drawable!!.toBitmap()

        // Get the original width and height
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // The aspect ratio (the width-to-height ratio) of the original icon is calculated to
        // ensure the icon maintains its original proportions when resized.
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

        // calculates the new height based on the provided width and the calculated aspect ratio,
        // ensuring that the icon retains its aspect ratio.
        val newHeight = (width / aspectRatio).toInt()

        // Resize the bitmap to maintain the aspect ratio
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, newHeight, false)

        // resized bitmap is then converted into a BitmapDescriptor that can be used with
        // Google Maps markers
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }

    /**
     * Generates a random start point on the left side of the map, near the pickup location.
     * The start point is calculated using polar coordinates, with the added flexibility
     * of controlling the direction using a random angle.
     *
     * @param minDistance The minimum distance (in meters) from the pickup location for the random point.
     * @param maxDistance The maximum distance (in meters) from the pickup location for the random point.
     * @return A new [com.an.ridesim.model.LatLngPoint] representing the randomly generated start point.
     *
     * A random distance between `minDistance` and `maxDistance` is chosen to control how far
     * the start point will be from the pickup. The angle at which the new point is generated is
     * randomly chosen within the range of 45° to 90° (left side of the map). Using the polar
     * coordinates method, a random point is generated within the specified radius,
     * considering the random angle. The method correctly accounts for the Earth's curvature and
     * adjusts the latitude and longitude accordingly.
     *
     */
    fun generateRandomStartPoint(
        pickupPoint: LatLngPoint,
        minDistance: Double,
        maxDistance: Double
    ): LatLngPoint {
        // Generate a random distance between `minDistance` and `maxDistance` to control how far
        // the start point will be from the pickup
        val randomDistance = Random.nextDouble(minDistance, maxDistance)

        // Generate an angle between 45° and 90°, at which the new point is generated
        val randomAngle = (45..90).random()

        // Convert the random distance and angle to polar coordinates
        // (latitude and longitude adjustments)
        // Earth's radius in meters
        val earthRadius = 6371e3
        // Change in latitude (in radians)
        val deltaLat = randomDistance / earthRadius
        // Change in longitude
        val deltaLon = randomDistance / (earthRadius * cos(Math.PI * pickupPoint.latitude / 180))

        // Apply the random angle to adjust the direction of the new point
        // and convert angle to radians
        val angleInRadians = Math.toRadians(randomAngle.toDouble())
        // Adjust latitude based on the angle
        val adjustedDeltaLat = deltaLat * cos(angleInRadians)
        // Adjust longitude based on the angle
        val adjustedDeltaLon = deltaLon * sin(angleInRadians)

        // Calculate the new latitude and longitude by adding the changes to the current position.
        // Convert deltaLat and deltaLon to degrees
        val newLat = pickupPoint.latitude + adjustedDeltaLat * 180 / Math.PI
        val newLon = pickupPoint.longitude + adjustedDeltaLon * 180 / Math.PI

        // Return the new LatLngPoint as the randomly generated start point
        return LatLngPoint(newLat, newLon)
    }
}
