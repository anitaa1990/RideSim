package com.an.ridesim.module

import android.content.Context
import com.an.ridesim.util.LocationUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LocationModule {
    @Provides
    fun provideLocationUtils(
        @ApplicationContext context: Context
    ): LocationUtils = LocationUtils(context)
}