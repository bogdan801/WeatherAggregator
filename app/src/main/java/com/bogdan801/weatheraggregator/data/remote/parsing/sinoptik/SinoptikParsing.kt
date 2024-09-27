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

        val currentTemperature = (baseDocument.getElementsByClass("_6fYCPKSx")[0].childNodes()[0] as TextNode).text().filter { it == '-' || it.isDigit() }.toInt()

        val iconSrc = baseDocument.getElementsByClass("kby+TyNs")[0].attributes()["src"]
        val currentSkyCondition = getSkyConditionFromSinoptik(iconSrc.substring(iconSrc.lastIndex-12..iconSrc.lastIndex-9))

        val days = mutableListOf<DayWeatherCondition>()
        for (i in 0..4){
            val date = currentDate + DatePeriod(days = i)
            val document = Jsoup
                .connect("$url/$date")
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .get()


            val dayTopPanel = document.getElementsByClass("D5LKqju5")[0].childNodes()[i]

            val skyDescriptor = decryptIconDescriptor(dayTopPanel.childNodes()[3].childNodes()[0].attributes()["class"].split(" ")[1])
            val daySkyCondition = getSkyConditionFromSinoptik(skyDescriptor)
            val dayTemperature = (dayTopPanel.childNodes()[4].childNodes()[1].childNodes()[1].childNodes()[0] as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()
            val nightTemperature = (dayTopPanel.childNodes()[4].childNodes()[0].childNodes()[1].childNodes()[0] as TextNode).text().filter { it.isDigit() || it == '-' }.toInt()

            val slices = mutableListOf<WeatherSlice>()

            val weatherDetails = document.getElementsByClass("mK1PSQn1")[0]

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
    if(sinoptikDescriptor.length != 4) throw ParsingException("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")
    if(sinoptikDescriptor[0] != 'd' && sinoptikDescriptor[0] != 'n') throw ParsingException("Invalid Sinoptik sky descriptor: '${sinoptikDescriptor[0]}'")
    if(sinoptikDescriptor.filter { it.isDigit() }.length != 3) throw ParsingException("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")
    if(sinoptikDescriptor[1] == '0' && (sinoptikDescriptor[2] != '0' || sinoptikDescriptor[3] != '0'))  throw ParsingException("Invalid Sinoptik sky descriptor: $sinoptikDescriptor")

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
            "mARr-SuW" -> "000"
            "CeTPIiz1" -> "100"
            "_0En556HG" -> "103"
            "_5jY1iL1b" -> "110"
            "epAkb3+y" -> "111"
            "ZyLiEuGi" -> "112"
            "A6rNPZa3" -> "120"
            "lYvGD-O+" -> "121"
            "C3OFN+pw" -> "122"
            "jJGQRRaI" -> "130"
            "_77vF1hsx" -> "131"
            "inrriluk" -> "132"
            "Qt4XNPSI" -> "140"
            "bkWVwna1" -> "141"
            "_2KU98Is+" -> "142"
            "ZUUj4KIC" -> "200"
            "--KV7Mu5" -> "210"
            "CoQJVDvm" -> "211"
            "nSX3SRap" -> "212"
            "Nh7Vm1Yj" -> "220"
            "NC+AzP3+" -> "221"
            "epgsWMkF" -> "222"
            "HAQxuG3q" -> "230"
            "n3Fh0MLz" -> "231"
            "_4WiizYF+" -> "232"
            "gbqEUD52" -> "240"
            "ELzveezc" -> "241"
            "p-7HfLSk" -> "242"
            "FRfVLUBE" -> "300"
            "BxZpnMNW" -> "310"
            "ujTcWkwz" -> "311"
            "huhh8w0M" -> "312"
            "XHhDosZ1" -> "320"
            "b1x46Fkb" -> "321"
            "i6T8OEuE" -> "322"
            "xbDCDxN5" -> "330"
            "_9Xede4hH" -> "331"
            "jmCqP6R4" -> "332"
            "H0vGEESC" -> "340"
            "MfWAgA8v" -> "341"
            "TK3eWMdo" -> "342"
            "RUdvrp93" -> "400"
            "sIgT9+8U" -> "410"
            "eq2UxJmz" -> "411"
            "_0GG3WOxj" -> "412"
            "Z2umynFF" -> "420"
            "VzrbN4ev" -> "421"
            "LI2zthRv" -> "422"
            "Uw0gN5+s" -> "430"
            "cgKcHrDD" -> "431"
            "hc0wM4jo" -> "432"
            "GIekUyaE" -> "440"
            "_6SsGfCP8" -> "441"
            "lDpWGRbY" -> "442"
            "xKt-30Ew" -> "500"
            "HqSyHbck" -> "600"
            else -> "000"
        }
    }
    else {
        when(input){
            "_3qxUBVmk" -> "000"
            "_5nCj7Uqa" -> "100"
            "gWGRRmob" -> "103"
            "_4Z8P78ae" -> "110"
            "fEaTteXJ" -> "111"
            "wKHYDDcL" -> "112"
            "ucGqEanU" -> "120"
            "bMFcRHFF" -> "121"
            "trQO3IjU" -> "122"
            "EirU3t2w" -> "130"
            "njaKCinm" -> "131"
            "+l5q1o2Q" -> "132"
            "ipu3eVm3" -> "140"
            "ED3HbrFP" -> "141"
            "_1fTB63SG" -> "142"
            "YBtWlRNQ" -> "200"
            "U0C-qqAg" -> "210"
            "QfB3RUrq" -> "211"
            "_5aAqo7kT" -> "212"
            "dtAUachG" -> "220"
            "_1XCr2H9k" -> "221"
            "OM-b0cp7" -> "222"
            "uRNA8rbW" -> "230"
            "kzU5a0Hn" -> "231"
            "jl+hoB5l" -> "232"
            "auMWfCOu" -> "240"
            "ScBIrB+c" -> "241"
            "ViKHDhO0" -> "242"
            "OIpYZgRB" -> "300"
            "UCBrppyQ" -> "310"
            "_1Blb3ELy" -> "311"
            "etq3tO5h" -> "312"
            "ow10W+rs" -> "320"
            "XUlB-x0o" -> "321"
            "KcDc6Nph" -> "322"
            "OXNz0hG7" -> "330"
            "to53ItZd" -> "331"
            "bpOHc0C-" -> "332"
            "erQzfPBT" -> "340"
            "fQY0rO0i" -> "341"
            "BP0XMWyD" -> "342"
            "OPA2VJPj" -> "400"
            "Oqyd4tVk" -> "410"
            "jr7YF526" -> "411"
            "cZi-mhve" -> "412"
            "J5Yif+YJ" -> "420"
            "q856VOg5" -> "421"
            "BikLrhTD" -> "422"
            "dle+K+CW" -> "430"
            "FvSuS6Zq" -> "431"
            "TODqwaUS" -> "432"
            "gSl5Sumg" -> "440"
            "-Zk1gal3" -> "441"
            "lBuC1ZI8" -> "442"
            "unEfI11b" -> "500"
            "-izVNJaS" -> "600"
            else -> "000"
        }
    }
    return day + code
}

fun decryptWindDirection(input: String): Int = when(input) {
    "h3+v9bod" -> 0
    "tbzCV4Ko" -> 1
    "Qj2IQFSf" -> 2
    "EIlfcmW3" -> 3
    "kfTsjNwW" -> 4
    "dmBec6hW" -> 5
    "_8S3rzjg6" -> 6
    "vxDsBpVV" -> 7
    else -> 0
}