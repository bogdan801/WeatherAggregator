package com.bogdan801.weatheraggregator.data.remote.api.dto.weather

data class OpenWeatherDto(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<TimeStamp>,
    val message: Int
)