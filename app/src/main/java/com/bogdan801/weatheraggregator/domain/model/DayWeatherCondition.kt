package com.bogdan801.weatheraggregator.domain.model

import kotlinx.datetime.LocalDate

data class DayWeatherCondition(
    val dayID: Int = 0,
    val dataID: Int = 0,
    val date: LocalDate = LocalDate(2022, 0, 1),
    val skyCondition: SkyCondition = SkyCondition("c_c_0_d"),
    val dayTemperature: Int = 0,
    val nightTemperature: Int = 0,
    val weatherByHours: List<WeatherSlice> = listOf()
)
