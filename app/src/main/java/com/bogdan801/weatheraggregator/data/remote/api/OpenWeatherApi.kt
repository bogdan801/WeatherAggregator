package com.bogdan801.weatheraggregator.data.remote.api

import com.bogdan801.weatheraggregator.data.remote.api.dto.location.LocationDto
import com.bogdan801.weatheraggregator.data.remote.api.dto.weather.OpenWeatherDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenWeatherApi {
    @GET("/data/2.5/forecast")
    suspend fun getWeatherData(@Query("lat")latitude: String, @Query("lon")longitude: String,  @Query("units") units: String, @Query("appid")apiKey: String): OpenWeatherDto

    @GET("/geo/1.0/direct")
    suspend fun getLocationInfo(@Query("q")name: String, @Query("appid")apiKey: String): List<LocationDto>
}