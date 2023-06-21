package com.bogdan801.weatheraggregator.data.remote.api

import androidx.annotation.Keep
import com.bogdan801.weatheraggregator.data.remote.api.dto.weather.OpenWeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

@Keep
interface OpenWeatherApi {
    @GET("/data/2.5/forecast")
    suspend fun getWeatherData(@Query("lat")latitude: String, @Query("lon")longitude: String,  @Query("units") units: String, @Query("appid")apiKey: String): OpenWeatherDto
}