package com.bogdan801.weatheraggregator.data.remote.parsing.sinoptik

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.ParsingException
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
import kotlin.math.roundToInt

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

        val currentState = baseDocument
            .childNodes()[1]
            .childNodes()[1]
            .childNodes()[0]
            .childNodes()[0]
            .childNodes()[5]
            .childNodes()[0]
            .childNodes()[1]
            .childNodes()[0]
            .childNodes()[1]

        val currentTemperature = (currentState.childNodes()[2].childNodes()[0] as TextNode).text().filter { it == '-' || it.isDigit() }.toInt()
        //val currentTemperature = (baseDocument.getElementsByClass("_6fYCPKSx")[0].childNodes()[0] as TextNode).text().filter { it == '-' || it.isDigit() }.toInt()

        //val iconSrc = baseDocument.getElementsByClass("kby+TyNs")[0].attributes()["src"]
        val iconSrc = currentState.childNodes()[0].childNodes()[1].attributes()["src"]
        val d = iconSrc.split("/").last().split("-")[0].padEnd(4, '0')
        val currentSkyCondition = getSkyConditionFromSinoptik(d)

        val days = mutableListOf<DayWeatherCondition>()
        for (i in 0..4){
            val date = currentDate + DatePeriod(days = i)
            val document = Jsoup
                .connect("$url/$date")
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get()


            val dayTopPanel = document.getElementsByClass("DMP0kolW")[0].childNodes()[i]

            val skyDescriptor = decryptIconDescriptor(dayTopPanel.childNodes()[3].childNodes()[0].attributes()["class"].split(" ")[1])
            val daySkyCondition = getSkyConditionFromSinoptik(skyDescriptor)
            val dayTemperature = (dayTopPanel.childNodes()[4].childNodes()[1].childNodes()[1].childNodes()[0] as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()
            val nightTemperature = (dayTopPanel.childNodes()[4].childNodes()[0].childNodes()[1].childNodes()[0] as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()

            val slices = mutableListOf<WeatherSlice>()

            val weatherDetails = document.getElementsByClass("iC5eqyQP")[0]

            val timeRow = weatherDetails.childNodes()[1].childNodes()[1]
            val temperatureRow = weatherDetails.childNodes()[2].childNodes()[1]
            val iconsRow = weatherDetails.childNodes()[2].childNodes()[0]
            val precipitationProbabilityRow = weatherDetails.childNodes()[2].childNodes()[6]
            val pressureRow = weatherDetails.childNodes()[2].childNodes()[3]
            val humidityRow = weatherDetails.childNodes()[2].childNodes()[4]
            val windRow = weatherDetails.childNodes()[2].childNodes()[5]

            timeRow.childNodes().forEachIndexed { index, node ->
                if(node is Element){
                    val time = node.text().filter { it != ' ' }
                    val temperature = (temperatureRow.childNodes()[index].childNodes()[0] as TextNode).text().filter { it.isDigit() ||  it == '-' }.toInt()
                    val descriptor = decryptIconDescriptor(
                        input = iconsRow.childNodes()[index].childNodes()[0].childNodes()[0].attributes()["class"].split(" ").last(),
                        isDay = time.split(":")[0].toInt() in 9..21,
                        isSmall = true
                    )
                    val skyCondition = getSkyConditionFromSinoptik(descriptor)
                    val precipitationProbability = (precipitationProbabilityRow.childNodes()[index].childNodes()[0] as TextNode).text()
                    val pressure = (pressureRow.childNodes()[index].childNodes()[0] as TextNode).text().toInt()
                    val humidity = (humidityRow.childNodes()[index].childNodes()[0] as TextNode).text().toInt()
                    val windPow = (windRow.childNodes()[index].childNodes()[1] as TextNode).text().toDouble().roundToInt()
                    val windDir = decryptWindDirection(windRow.childNodes()[index].childNodes()[0].attributes()["class"].split(" ")[1])
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
    catch (e: Exception){
        println(e.message)
        throw e
    }

}

private suspend fun getSkyConditionFromSinoptik(sinoptikDescriptor: String): SkyCondition {
    println(sinoptikDescriptor)
    if(sinoptikDescriptor.length != 4) throw ParsingException("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")
    if(sinoptikDescriptor[0] != 'd' && sinoptikDescriptor[0] != 'n') throw ParsingException("Invalid Sinoptik sky descriptor: '${sinoptikDescriptor[0]}'")
    if(sinoptikDescriptor.filter { it.isDigit() }.length != 3) throw ParsingException("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")
    if(sinoptikDescriptor[1] == '0' && (sinoptikDescriptor[2] != '0' || sinoptikDescriptor[3] != '0')) throw ParsingException("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")

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

fun decryptIconDescriptor(input: String, isDay: Boolean = true, isSmall: Boolean = false): String {
    val day = if(isDay) "d" else "n"
    val code = if(isSmall){
        when(input){
            "Y4vZD9qs" -> "000"
            "cZLpxG-i" -> "100"
            "qAXHVbEx" -> "103"
            "W5Saxw6z" -> "110"
            "TB82NYm3" -> "111"
            "Sw5E4k8z" -> "112"
            "i+EFhaNa" -> "120"
            "Vjt2xQi3" -> "121"
            "S12PYG6C" -> "122"
            "_4seFxLui" -> "130"
            "R7P2gycn" -> "131"
            "b+pqjIat" -> "132"
            "L8V8ARHi" -> "140"
            "o7Fik5zM" -> "141"
            "gCxa42NF" -> "142"
            "Yiu4p5pr" -> "200"
            "HMLbBzuU" -> "210"
            "JsaQmfCg" -> "211"
            "QNnTc-A9" -> "212"
            "rXPRUSWq" -> "220"
            "nvBqO-h3" -> "221"
            "_59RY-K7U" -> "222"
            "JZsniVMw" -> "230"
            "H5DCoyR4" -> "231"
            "rOPf1Zz0" -> "232"
            "rkGtQNN-" -> "240"
            "omQQdx1k" -> "241"
            "Fwa6T8au" -> "242"
            "B+NuAG9v" -> "300"
            "_3SVMeRHh" -> "310"
            "lAkiJCl1" -> "311"
            "ygGKQp0x" -> "312"
            "Y167c-Ac" -> "320"
            "PuBjqv8s" -> "321"
            "XCQGMZzD" -> "322"
            "+btGxFqY" -> "330"
            "NU5IUrDO" -> "331"
            "zf776BVa" -> "332"
            "IjZnLnEY" -> "340"
            "XbciYV58" -> "341"
            "UAhEm13E" -> "342"
            "UFEKK6vx" -> "400"
            "YvCnslnn" -> "410"
            "_82WkSwYw" -> "411"
            "QzMGtwtU" -> "412"
            "D0w9l8zR" -> "420"
            "Zsic8w8T" -> "421"
            "-W+ZTdAD" -> "422"
            "JJPJLvCM" -> "430"
            "Yzh4WQ0Y" -> "431"
            "vKcGltmr" -> "432"
            "QKSU98eA" -> "440"
            "gH8qY1Mp" -> "441"
            "N3fS6X3n" -> "442"
            "WDcEBW5-" -> "500"
            "_39a89iwO" -> "600"
            else -> "000"
        }
    }
    else {
        when(input){
            "HuKZu6Vq" -> "000"
            "XAPDdeWb" -> "100"
            "ORcsYnzi" -> "103"
            "hIsY5K1-" -> "110"
            "i7VN1kaC" -> "111"
            "hTm+j-co" -> "112"
            "i7UMyAET" -> "120"
            "I2IPqIUv" -> "121"
            "_1JsImpxh" -> "122"
            "_7lVKyeSo" -> "130"
            "vYehKRe+" -> "131"
            "qmrY1wig" -> "132"
            "XWkOeHVd" -> "140"
            "yHIvuMk9" -> "141"
            "vfKriKf9" -> "142"
            "fTlrxLKq" -> "200"
            "_6-D3FqXC" -> "210"
            "MPgmkOIG" -> "211"
            "_5ZPonkEp" -> "212"
            "_8Ze+JHip" -> "220"
            "+6ZDliZy" -> "221"
            "AIL9S5v6" -> "222"
            "iOEVWM9X" -> "230"
            "NTF9KcX6" -> "231"
            "NCzWRqr-" -> "232"
            "TOiDEZr5" -> "240"
            "jMeLsvOF" -> "241"
            "_3ZP7mVKX" -> "242"
            "lUUten9f" -> "300"
            "_5Yfop-55" -> "310"
            "VJK3IHKS" -> "311"
            "rvacyS7p" -> "312"
            "pRnGDksx" -> "320"
            "tnt9D2uH" -> "321"
            "_0sse999u" -> "322"
            "oW7V-GBL" -> "330"
            "_8h8QObS1" -> "331"
            "er6WeH-X" -> "332"
            "B42Qiniq" -> "340"
            "Dz9xrxo+" -> "341"
            "ueZtyWTT" -> "342"
            "N5FMbQtj" -> "400"
            "XJXMdpgT" -> "410"
            "urO5TZKb" -> "411"
            "m5scv-s0" -> "412"
            "_1r7kWZh1" -> "420"
            "IQBXDsW7" -> "421"
            "_9aP+TT-V" -> "422"
            "s6k5cGbA" -> "430"
            "PNn7Jy9W" -> "431"
            "BFQIgPCo" -> "432"
            "_9TZwchLj" -> "440"
            "_4-RftSYV" -> "441"
            "XGrWw4Te" -> "442"
            "_9o9j6Qhj" -> "500"
            "IxOLCQoD" -> "600"
            else -> "000"
        }
    }
    return day + code
}

fun decryptWindDirection(input: String): Int = when(input) {
    "k9cUkpUK" -> 0
    "tQoAoJaU" -> 1
    "APkHBCMM" -> 2
    "xYejJAKL" -> 3
    "fkJEemEn" -> 4
    "OF2yP-fN" -> 5
    "OxL5Wsqz" -> 6
    "MQSQlO1E" -> 7
    else -> 0
}