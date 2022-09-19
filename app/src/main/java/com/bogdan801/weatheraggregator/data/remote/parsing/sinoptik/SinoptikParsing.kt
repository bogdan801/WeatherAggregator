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
    val baseUrl = "https://ua.sinoptik.ua/"
    val url = baseUrl + sLocation.link

    val baseDocument = Jsoup
        .connect(url)
        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
        .referrer("https://www.google.com")
        .get()

    val currentDate = getCurrentDate()
    val currentLocation = location.name
    val domain = WeatherSourceDomain.Sinoptik

    val currentTemperature = 0
    val currentSkyCondition = SkyCondition()

    val days = mutableListOf<DayWeatherCondition>()

    for (i in 0..4){
        val date = currentDate + DatePeriod(days = i)
        val document = Jsoup
            .connect("$url/$date")
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("https://www.google.com")
            .get()

        val contents = document.getElementsByClass("city__forecast-col")
        val dayCard = baseDocument.getElementById(date.toString())

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
        currentSkyCondition = currentSkyCondition,
        currentTemperature = currentTemperature,
        weatherByDates = days.toList()
    )
}

private fun getSkyConditionFromSinoptik(sinoptikDescriptor: String): SkyCondition {
    TODO()
}