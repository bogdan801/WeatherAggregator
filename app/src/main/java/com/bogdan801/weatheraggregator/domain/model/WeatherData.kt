package com.bogdan801.weatheraggregator.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import kotlinx.datetime.LocalDate
import com.bogdan801.weatheraggregator.R

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
    @Composable
    fun getDomainPainter(): Painter = when(domain){
        WeatherSourceDomain.Meta -> painterResource(id = R.drawable.ic_meta)
        WeatherSourceDomain.Sinoptik -> painterResource(id = R.drawable.ic_sinoptik)
        WeatherSourceDomain.OpenWeather -> painterResource(id = R.drawable.ic_open_weather)
        WeatherSourceDomain.Average -> painterResource(id = R.drawable.ic_average)
    }
}


enum class WeatherSourceDomain(val domain: String) {
    Meta("https://pogoda.meta.ua"),
    Sinoptik("https://ua.sinoptik.ua"),
    OpenWeather("https://api.openweathermap.org"),
    Average("")
}