package com.an.ridesim.module

import android.content.Context
import com.an.ridesim.util.LocationUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocationModule {
    @Provides
    @Singleton
    fun provideLocationUtils(
        @ApplicationContext context: Context
    ): LocationUtils = LocationUtils(context)
}