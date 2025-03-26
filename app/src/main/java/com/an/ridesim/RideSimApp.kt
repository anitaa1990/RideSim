package com.an.ridesim

import android.app.Application
import com.an.ridesim.BuildConfig.MAPS_API_KEY
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RideSimApp: Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Places SDK with your API key
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, MAPS_API_KEY)
        }
    }
}