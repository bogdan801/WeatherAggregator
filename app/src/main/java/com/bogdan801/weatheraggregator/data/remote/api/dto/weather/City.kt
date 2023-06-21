package com.bogdan801.weatheraggregator.data.remote.api.dto.weather

import androidx.annotation.Keep

@Keep
data class City(
    val coord: Coord,
    val country: String,
    val id: Int,
    val name: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int
)