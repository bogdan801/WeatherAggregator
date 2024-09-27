package com.bogdan801.weatheraggregator.data.remote.parsing.meta

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.ParsingException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import java.net.UnknownHostException

suspend fun getWeatherDataFromMeta(location: Location): WeatherData = withContext(Dispatchers.IO)  {
    val baseUrl = "https://pogoda.meta.ua"
    val url = baseUrl + location.metaLink

    try {
        val baseDocument = Jsoup
            .connect(url)
            .timeout(15000)
            .userAgent("Mozilla")
            .header("Cookie", "PHPSESSID=6d9v48p1i5lpgm7pi75ag0okvq; currency=INR; magnitude=LC; ad=ee5ceff9a39de83a4dfcd9cc96efd7aa04912966; ad=ee5ceff9a39de83a4dfcd9cc96efd7aa04912966; wec=296642702; nobtlgn=714443298; ac=68156306%7C526294102%7C424761678; ac=68156306%7C526294102%7C424761678; _gcl_au=1.1.443352322.1663577511; _gid=GA1.2.307799405.1663577512; _fbp=fb.1.1663577512204.684634655; _clck=fgstbv|1|f50|0; __gads=ID=5ed6c786ebad7ee2-2263761e9bd600b6:T=1663577512:S=ALNI_Mb0p_Gif2EChNCORy7JOdTU7x4kjA; __gpi=UID=000009ce9ba54907:T=1663577512:RT=1663577512:S=ALNI_MbPL5xoHxgY76gU9mzDzJltULL80Q; __cf_bm=iIVI9aabT4vAdAmvQQzQTDDs9z4MPaMB1gv602Vn2rI-1663577514-0-AQ9ZKhXneLwVKm6CKEzLoY2EKcrIlNB82wgEPDw7taV6k/fnqTzp0L5zrpAl0fnkF1dn7Ac1DyNdfOnsgCTjBZx5Y6ia4Pvj2ceyIBfyXcIYpR8JkYTYGHfqPlrncv7k6Q==; alp=VROL; aa=364476%7C230264168%7C651860858; aa=364476%7C230264168%7C651860858; arl=604590238; arl=604590238; PERMA-ALERT=0; pgv=6; _ga=GA1.1.1410692956.1663577512; _ga_N9R425YFBJ=GS1.1.1663577511.1.1.1663577540.31.0.0; _clsk=1prm412|1663577540567|4|1|l.clarity.ms/collect")
            .method(Connection.Method.GET)
            .referrer("https://pogoda.meta.ua/ua")
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
                .timeout(15000)
                .userAgent("Mozilla")
                .header("Cookie", "PHPSESSID=6d9v48p1i5lpgm7pi75ag0okvq; currency=INR; magnitude=LC; ad=ee5ceff9a39de83a4dfcd9cc96efd7aa04912966; ad=ee5ceff9a39de83a4dfcd9cc96efd7aa04912966; wec=296642702; nobtlgn=714443298; ac=68156306%7C526294102%7C424761678; ac=68156306%7C526294102%7C424761678; _gcl_au=1.1.443352322.1663577511; _gid=GA1.2.307799405.1663577512; _fbp=fb.1.1663577512204.684634655; _clck=fgstbv|1|f50|0; __gads=ID=5ed6c786ebad7ee2-2263761e9bd600b6:T=1663577512:S=ALNI_Mb0p_Gif2EChNCORy7JOdTU7x4kjA; __gpi=UID=000009ce9ba54907:T=1663577512:RT=1663577512:S=ALNI_MbPL5xoHxgY76gU9mzDzJltULL80Q; __cf_bm=iIVI9aabT4vAdAmvQQzQTDDs9z4MPaMB1gv602Vn2rI-1663577514-0-AQ9ZKhXneLwVKm6CKEzLoY2EKcrIlNB82wgEPDw7taV6k/fnqTzp0L5zrpAl0fnkF1dn7Ac1DyNdfOnsgCTjBZx5Y6ia4Pvj2ceyIBfyXcIYpR8JkYTYGHfqPlrncv7k6Q==; alp=VROL; aa=364476%7C230264168%7C651860858; aa=364476%7C230264168%7C651860858; arl=604590238; arl=604590238; PERMA-ALERT=0; pgv=6; _ga=GA1.1.1410692956.1663577512; _ga_N9R425YFBJ=GS1.1.1663577511.1.1.1663577540.31.0.0; _clsk=1prm412|1663577540567|4|1|l.clarity.ms/collect")
                .method(Connection.Method.GET)
                .referrer("https://pogoda.meta.ua/ua")
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

private fun getSkyConditionFromMeta(metaDescriptor: String): SkyCondition {
    var parts = metaDescriptor.split("-")
    var isStorm = false
    if(parts.size != 3){
        if(parts.size == 4 && parts[0] == "storm") {
            parts = parts.subList(1, parts.size)
            isStorm = true
        }
        else throw ParsingException("Invalid Meta sky descriptor: $metaDescriptor")
    }

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
                else -> throw ParsingException("Invalid Meta sky descriptor: ${parts[1]}")
            }

            val precipitationType = parts[2].filter { !it.isDigit() }
            val precipitationLevel = if(isStorm) 6 else parts[2].last().digitToInt()

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
                else -> throw ParsingException("Invalid Meta sky descriptor: ${parts[1]}")
            }

            val precipitationType = parts[2].filter { !it.isDigit() }
            val precipitationLevel = if(isStorm) 6 else parts[2].last().digitToInt()

            val precipitation = when(precipitationType){
                "osad" -> Precipitation.None
                "rain" -> Precipitation.Rain(RainLevel.values()[precipitationLevel-1])
                "snow" -> Precipitation.Snow(SnowLevel.values()[precipitationLevel-1])
                else   -> Precipitation.None
            }
            SkyCondition(cloudiness, precipitation, timeOfDay)
        }
        else -> throw ParsingException("Invalid Meta sky descriptor: ${parts[0]}")
    }
}