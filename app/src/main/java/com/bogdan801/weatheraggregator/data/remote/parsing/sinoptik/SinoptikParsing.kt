package com.bogdan801.weatheraggregator.data.remote.parsing.sinoptik

import android.util.Log

import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode

fun getWeatherDataFromSinoptik(location: Location): WeatherData {
    val sLocation = location.toSinoptikLocation()
    val baseUrl = "https://ua.sinoptik.ua"
    val url = baseUrl + sLocation.link

    val baseDocument = Jsoup
        .connect(url)
        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
        .referrer("https://www.google.com")
        .get()

    val currentDate = getCurrentDate()
    val currentLocation = location.name
    val domain = WeatherSourceDomain.Sinoptik

    val currentTemperature = (baseDocument.getElementsByClass("today-temp")[0].childNodes()[0] as TextNode).text().filter { it == '-' || it.isDigit() }.toInt()

    val iconSrc = baseDocument.getElementsByClass("img")[0].childNodes()[1].attributes()["src"]
    val currentSkyCondition = getSkyConditionFromSinoptik(iconSrc.substring(iconSrc.lastIndex-7..iconSrc.lastIndex-4))

    val days = mutableListOf<DayWeatherCondition>()
    for (i in 0..4){
        val date = currentDate + DatePeriod(days = i)
        val document = Jsoup
            .connect("$url/$date")
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("https://www.google.com")
            .get()

        val contents = document.getElementsByClass("city__forecast-col")

        val daySkyCondition = SkyCondition()

        val dayTemperature = 0
        val nightTemperature = 0

        val slices = mutableListOf<WeatherSlice>()
        contents.forEach{ slice ->
            val time = "00:00"
            val skyCondition = SkyCondition()
            val temperature =  0
            val precipitationProbability = 0
            val pressure = 0
            val humidity = 0
            val windPower = 1
            val windDirection = "N"
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

    Log.d("puk", baseDocument.text())

    return WeatherData(
        currentDate = currentDate,
        currentLocation = currentLocation,
        domain = domain,
        url = url,
        currentSkyCondition = SkyCondition(),
        currentTemperature = currentTemperature,
        weatherByDates = days.toList()
    )
}

private fun getSkyConditionFromSinoptik(sinoptikDescriptor: String): SkyCondition {
    if(sinoptikDescriptor.length != 4) throw Exception("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")
    if(sinoptikDescriptor[0] != 'd' && sinoptikDescriptor[0] != 'n') throw Exception("Invalid Sinoptik sky descriptor: '${sinoptikDescriptor[0]}'")
    if(sinoptikDescriptor.filter { it.isDigit() }.length != 3) throw Exception("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")
    if(sinoptikDescriptor[1] == '0' && (sinoptikDescriptor[2] != '0' || sinoptikDescriptor[3] != '0'))  throw Exception("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")

    val timeOfDay= when(sinoptikDescriptor[0]){
        'd' -> TimeOfDay.Day
        'n' -> TimeOfDay.Night
        else -> TimeOfDay.Day
    }

    val cloudiness = when(sinoptikDescriptor[1]){
        '0' -> Cloudiness.Clear
        '1' -> Cloudiness.LittleCloudy
        '2' -> Cloudiness.CloudyWithClearing
        '3' -> Cloudiness.Cloudy
        '4' -> Cloudiness.Gloomy
        '5' -> Cloudiness.Clear
        '6' -> Cloudiness.Gloomy
        else -> Cloudiness.Clear
    }

    val precipitation: Precipitation = when(sinoptikDescriptor[3]){
        '0' -> when(sinoptikDescriptor[2]){
            '0' -> Precipitation.None
            '1' -> Precipitation.Rain(RainLevel.One)
            '2' -> Precipitation.Rain(RainLevel.Two)
            '3' -> Precipitation.Rain(RainLevel.Three)
            '4' -> Precipitation.Rain(RainLevel.Thunder)
            else -> Precipitation.None
        }
        '1' -> when(sinoptikDescriptor[2]){
            '0' -> Precipitation.None
            '1' -> Precipitation.RainWithSnow(RainWithSnowLevel.One)
            '2' -> Precipitation.RainWithSnow(RainWithSnowLevel.Two)
            '3' -> Precipitation.RainWithSnow(RainWithSnowLevel.Three)
            '4' -> Precipitation.RainWithSnow(RainWithSnowLevel.Thunder)
            else -> Precipitation.None
        }
        '2' -> when(sinoptikDescriptor[2]){
            '0' -> Precipitation.None
            '1' -> Precipitation.Snow(SnowLevel.One)
            '2' -> Precipitation.Snow(SnowLevel.Two)
            '3' -> Precipitation.Snow(SnowLevel.Three)
            '4' -> Precipitation.Snow(SnowLevel.Thunder)
            else -> Precipitation.None
        }
        else -> Precipitation.None
    }

    return SkyCondition(
        cloudiness,
        precipitation,
        timeOfDay
    )
}