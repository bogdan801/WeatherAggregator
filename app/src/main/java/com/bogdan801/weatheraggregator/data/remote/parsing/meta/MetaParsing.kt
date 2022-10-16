package com.bogdan801.weatheraggregator.data.remote.parsing.meta

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import java.net.UnknownHostException

fun getWeatherDataFromMeta(location: Location): WeatherData {
    val baseUrl = "https://pogoda.meta.ua"
    val url = baseUrl + location.link

    try {
        val baseDocument = Jsoup
            .connect(url)
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("https://www.google.com")
            .get()

        val currentDate = getCurrentDate()
        val currentLocation = location.name
        val domain = WeatherSourceDomain.Meta
        val currentTemperature = (baseDocument.getElementsByClass("city__main-temp")[0].childNodes()[0] as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()
        val currentSkyCondition = getSkyConditionFromMeta(baseDocument.getElementsByClass("city__main-image-icon")[0].childNodes()[1].attributes()["class"].substring(17))

        val days = mutableListOf<DayWeatherCondition>()
        for (i in 0..4){
            val date = currentDate + DatePeriod(days = i)
            val document = Jsoup
                .connect(url + "/${date}/ajax/")
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get()

            val contents = document.getElementsByClass("city__forecast-col")
            val dayCard = baseDocument.getElementById(date.toString())

            val daySkyCondition = getSkyConditionFromMeta(dayCard!!.getElementsByClass("city__day-image")[0].childNodes()[1].attributes()["class"].substring(6))

            val dayTemperature = (dayCard.getElementsByClass("city__day-temperature")[0].childNodes()[1].firstChild() as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()
            val nightTemperature = (dayCard.getElementsByClass("city__day-temperature")[0].childNodes()[3].firstChild() as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()

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
                val wind = Wind.create(windDirection, windPower)

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

            days.add(
                DayWeatherCondition(
                    date = date,
                    skyCondition = daySkyCondition,
                    dayTemperature = dayTemperature,
                    nightTemperature = nightTemperature,
                    weatherByHours = slices.toList()
                )
            )
        }

        return WeatherData(
            currentDate = currentDate,
            currentLocation = currentLocation,
            domain = domain,
            url = url,
            currentSkyCondition = currentSkyCondition,
            currentTemperature = currentTemperature,
            weatherByDates = days.toList()
        )
    }
    catch (e: UnknownHostException){
        throw NoConnectionException("No internet connection")
    }
    catch (e: HttpStatusException){
        throw WrongUrlException("Wrong URL. Status: ${e.statusCode}. URL: ${e.url}")
    }
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