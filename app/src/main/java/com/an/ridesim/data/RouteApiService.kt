package com.an.ridesim.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RouteApiService {

    @GET("directions/json")
    suspend fun getRoute(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Response<RouteApiResponse>
}