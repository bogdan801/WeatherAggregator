package com.bogdan801.weatheraggregator.domain.model

import kotlinx.datetime.LocalDate

data class WeatherData(
    val dataID: Int = 0,
    val currentDate: LocalDate = LocalDate(2022, 1, 1),
    val currentLocation: String = "Desnianske",
    val domain: WeatherSourceDomain = WeatherSourceDomain.Meta,
    val url: String = "https://pogoda.meta.ua/ua/Chernihivska/Koropskyi/Sverdlovka/",
    val currentSkyCondition: SkyCondition = SkyCondition("c_c_0_d"),
    val currentTemperature: Int = 0,
    var weatherByDates: List<DayWeatherCondition> = listOf()
){
    val isEmpty: Boolean get() = weatherByDates.isEmpty()
}


enum class WeatherSourceDomain(val domain: String) {
    Meta("https://pogoda.meta.ua/ua/"),
    Sinoptik("https://ua.sinoptik.ua/%D0%BF%D0%BE%D0%B3%D0%BE%D0%B4%D0%B0-"),
    OpenWeather("https://api.openweathermap.org"),
    Average("")
}