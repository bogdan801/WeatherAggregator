package com.bogdan801.weatheraggregator.presentation.screens.home

import com.bogdan801.weatheraggregator.domain.model.WeatherData

data class WeatherDataState(
    val data: WeatherData = WeatherData(),
    val isLoading: Boolean = false,
    val error: String? = null
)
