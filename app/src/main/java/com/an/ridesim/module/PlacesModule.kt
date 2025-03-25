package com.an.ridesim.module

import android.content.Context
import com.an.ridesim.BuildConfig
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {
    @Provides
    @Singleton
    fun providePlacesClient(
        @ApplicationContext context: Context
    ): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }
}