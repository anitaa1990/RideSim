package com.an.ridesim.module

import com.an.ridesim.data.RouteApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RouteApiModule {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    @Provides
    fun provideBaseUrl(): String = BASE_URL

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String): Retrofit {
        val interceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRouteApiService(retrofit: Retrofit): RouteApiService =
        retrofit.create(RouteApiService::class.java)
}