package com.bogdan801.weatheraggregator.data.remote.parsing

import android.util.Log
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.jsoup.Jsoup

class ParsingException(reason: String) : Exception(reason)


suspend fun getWeatherDataFromMeta(location: Location): WeatherData {
    val baseUrl = "https://pogoda.meta.ua"


    for (i in 0..4){
        val document = Jsoup
            .connect(baseUrl + location.link + "/${getCurrentDate()+DatePeriod(days = i)}/ajax/")
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("https://www.google.com")
            .get()

        Log.d("puk", document.text())
    }


    return WeatherData()
}

suspend fun getWeatherDataFromSinoptik(location: Location): WeatherData{
    return WeatherData()
}




