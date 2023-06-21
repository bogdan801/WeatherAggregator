package com.bogdan801.weatheraggregator.data.remote.api.dto.weather

import androidx.annotation.Keep
import com.bogdan801.weatheraggregator.data.util.toFormattedTime
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Keep
data class TimeStamp(
    val clouds: Clouds,
    val dt: Int,
    val dt_txt: String,
    val main: Main,
    val pop: Double,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: WindInfo
){
    fun toWeatherSlice() = WeatherSlice(
        time = Instant
            .fromEpochMilliseconds(dt.toLong()*1000)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toFormattedTime(),
        temperature = main.temp.toInt(),
        skyCondition = weather[0].toSkyCondition(),
        precipitationProbability = (pop*100).toInt(),
        pressure = (main.pressure.toDouble() / 1.33322387415).toInt(),
        humidity = main.humidity,
        wind = wind.toWind()
    )

}