package com.bogdan801.weatheraggregator.data.remote.parsing.sinoptik

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.UnknownHostException

suspend fun getWeatherDataFromSinoptik(location: Location): WeatherData = withContext(Dispatchers.IO) {
    val url = "https://ua.sinoptik.ua" + location.sinoptikLink

    try {
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


            val dayTopPanel = document.getElementById("bd${i+1}")!!

            val daySkyCondition = getSkyConditionFromSinoptik(dayTopPanel.getElementsByClass("weatherIco")[0].attributes()["class"].substring(11))
            val dayTemperature = (dayTopPanel.getElementsByClass("max")[0].childNodes()[1].childNodes()[0] as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()
            val nightTemperature = (dayTopPanel.getElementsByClass("min")[0].childNodes()[1].childNodes()[0] as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()

            val slices = mutableListOf<WeatherSlice>()

            val weatherDetails = document.getElementsByClass("weatherDetails")[0].childNodes()[3]

            val timeRow = weatherDetails.childNodes()[1]
            val temperatureRow = weatherDetails.childNodes()[5]
            val iconsRow = weatherDetails.childNodes()[3]
            val precipitationProbabilityRow = weatherDetails.childNodes()[15]
            val pressureRow = weatherDetails.childNodes()[9]
            val humidityRow = weatherDetails.childNodes()[11]
            val windRow = weatherDetails.childNodes()[13]

            timeRow.childNodes().forEachIndexed { index, node ->
                if(node is Element){
                    val time = node.text().filter { it != ' ' }
                    if(time == "15:00"){
                        println()
                    }
                    val temperature = (temperatureRow.childNodes()[index].childNodes()[0] as TextNode).text().filter { it.isDigit() ||  it == '-' }.toInt()
                    val skyCondition = getSkyConditionFromSinoptik(iconsRow.childNodes()[index].childNodes()[1].attributes()["class"].substring(11))
                    val precipitationProbability = (precipitationProbabilityRow.childNodes()[index].childNodes()[0] as TextNode).text()
                    val pressure = (pressureRow.childNodes()[index].childNodes()[0] as TextNode).text().toInt()
                    val humidity = (humidityRow.childNodes()[index].childNodes()[0] as TextNode).text().toInt()
                    val windDir = windRow.childNodes()[index].childNodes()[1].attributes()["class"].toString().split('-')[1]
                    val windPow = (windRow.childNodes()[index].childNodes()[1].childNodes()[0] as TextNode).text().toDouble().toInt()
                    slices.add(
                        WeatherSlice(
                            time = if(time.length!=5) "0$time" else time,
                            temperature = temperature,
                            skyCondition = skyCondition,
                            precipitationProbability = if (precipitationProbability == "-") -1 else precipitationProbability.toInt(),
                            pressure = pressure,
                            humidity = humidity,
                            wind = Wind.create(windDir, windPow)
                        )
                    )
                }
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

        return@withContext WeatherData(
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

private suspend fun getSkyConditionFromSinoptik(sinoptikDescriptor: String): SkyCondition {
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