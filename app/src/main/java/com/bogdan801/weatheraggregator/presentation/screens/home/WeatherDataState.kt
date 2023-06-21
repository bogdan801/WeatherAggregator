package com.bogdan801.weatheraggregator.presentation.screens.home

import androidx.annotation.Keep
import com.bogdan801.weatheraggregator.domain.model.WeatherData

@Keep
sealed class WeatherDataState(
    val data: WeatherData = WeatherData(),
    val isLoading: Boolean = false,
    val error: String? = null
){
    data class Data(val d: WeatherData): WeatherDataState(d, false, null)
    data class IsLoading(val d: WeatherData = WeatherData()): WeatherDataState(d, true, null)
    data class Error(val d: WeatherData, val message: String): WeatherDataState(d, false, message)
}
