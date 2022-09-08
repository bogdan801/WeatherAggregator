package com.bogdan801.weatheraggregator.data.remote.parsing

import com.bogdan801.weatheraggregator.domain.model.WeatherData

suspend fun getWeatherDataFromMeta(location: String): WeatherData {
    return WeatherData()
}

suspend fun getWeatherDataFromSinoptik(location: String): WeatherData{
    return WeatherData()
}