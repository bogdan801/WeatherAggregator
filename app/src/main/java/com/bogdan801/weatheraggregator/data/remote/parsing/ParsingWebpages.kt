package com.bogdan801.weatheraggregator.data.remote.parsing

import android.util.Log
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode

class ParsingException(reason: String) : Exception(reason)


suspend fun getWeatherDataFromMeta(location: Location): WeatherData {
    val baseUrl = "https://pogoda.meta.ua"

    val days = mutableListOf<DayWeatherCondition>()
    for (i in 0..4){
        val document = Jsoup
            .connect(baseUrl + location.link + "/${getCurrentDate() + DatePeriod(days = i)}/ajax/")
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("https://www.google.com")
            .get()

        val contents = document.getElementsByClass("city__forecast-col")
        val slices = mutableListOf<WeatherSlice>()

        contents.forEach{ slice ->
            val time = (slice.getElementsByClass("city__forecast-time")[0].childNodes()[0] as TextNode).text()
            val skyCondition = getSkyConditionFromMeta(slice.getElementsByClass("city__forecast-icon icon")[0].childNodes()[1].attributes()["class"].substring(6))
            val temperature = (slice.getElementsByClass("graph-data__value")[0].childNodes()[0] as TextNode).text().filter { it != ' ' && it != '+' }.toInt()
            val precipitationProbability = (slice.getElementsByClass("city__forecast-precipitationProbability")[0].childNodes()[0] as TextNode).text().toInt()
            val pressure = (slice.getElementsByClass("city__forecast-pressure")[0].childNodes()[0] as TextNode).text().toInt()
            val humidity = (slice.getElementsByClass("city__forecast-humidity")[0].childNodes()[0] as TextNode).text().toInt()
            val windPower = (slice.getElementsByClass("city__forecast-wind")[0].childNodes()[0] as TextNode).text().filter { it != ' ' }.toInt()
            val windDirection = slice.getElementsByClass("city__forecast-wind")[0].childNodes()[1].attributes()["title"]
            val wind = Wind.get(windDirection, windPower)

            slices.add(
                WeatherSlice(
                    time = time,
                    skyCondition = skyCondition,
                    temperature = temperature,
                    precipitationProbability = precipitationProbability,
                    pressure = pressure,
                    humidity = humidity,
                    wind = wind
                )
            )
        }
    }

    return WeatherData()
}

private fun getSkyConditionFromMeta(metaDescriptor: String): SkyCondition {
    val parts = metaDescriptor.split("-")
    if(parts.size != 3) throw Exception("Invalid Meta sky descriptor: $metaDescriptor")

    val cloudLevel = parts[1].last().digitToInt()

    return when(parts[0]){
        "day" -> {
            val timeOfDay = TimeOfDay.Day
            val cloudiness = when(cloudLevel) {
                0 -> Cloudiness.Clear
                1 -> Cloudiness.LittleCloudy
                2 -> Cloudiness.LittleCloudy
                3 -> Cloudiness.CloudyWithClearing
                4 -> Cloudiness.Cloudy
                5 -> Cloudiness.Gloomy
                else -> throw Exception("Invalid Meta sky descriptor: ${parts[1]}")
            }

            val precipitationType = parts[2].filter { !it.isDigit() }
            val precipitationLevel = parts[2].last().digitToInt()

            val precipitation = when(precipitationType){
                "osad" -> Precipitation.None
                "rain" -> Precipitation.Rain(RainLevel.values()[precipitationLevel-1])
                "snow" -> Precipitation.Snow(SnowLevel.values()[precipitationLevel-1])
                else   -> Precipitation.None
            }

            SkyCondition(cloudiness, precipitation, timeOfDay)
        }
        "night" -> {
            val timeOfDay = TimeOfDay.Night
            val cloudiness = when(cloudLevel) {
                0 -> Cloudiness.Clear
                1 -> Cloudiness.LittleCloudy
                2 -> Cloudiness.LittleCloudy
                3 -> Cloudiness.CloudyWithClearing
                4 -> Cloudiness.CloudyWithClearing
                5 -> Cloudiness.CloudyWithClearing
                else -> throw Exception("Invalid Meta sky descriptor: ${parts[1]}")
            }

            val precipitationType = parts[2].filter { !it.isDigit() }
            val precipitationLevel = parts[2].last().digitToInt()

            val precipitation = when(precipitationType){
                "osad" -> Precipitation.None
                "rain" -> Precipitation.Rain(RainLevel.values()[precipitationLevel-1])
                "snow" -> Precipitation.Snow(SnowLevel.values()[precipitationLevel-1])
                else   -> Precipitation.None
            }

            SkyCondition(cloudiness, precipitation, timeOfDay)
        }
        else -> throw Exception("Invalid Meta sky descriptor: ${parts[0]}")
    }
}

suspend fun getWeatherDataFromSinoptik(location: Location): WeatherData{
    return WeatherData()
}