package com.bogdan801.weatheraggregator.data.remote.api.dto.weather

import androidx.annotation.Keep

@Keep
data class Coord(
    val lat: Double,
    val lon: Double
)