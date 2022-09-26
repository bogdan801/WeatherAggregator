package com.bogdan801.weatheraggregator.presentation.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import com.bogdan801.weatheraggregator.domain.model.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.WeatherData

@Composable
fun WeatherDataViewer(
    modifier: Modifier = Modifier,
    data: WeatherData
){
    Box(modifier = modifier.fillMaxWidth().background(Color.White)){
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(96.dp),
                painter = data.currentSkyCondition.getPainterResource(),
                contentDescription = ""
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = data.currentLocation + " - " + data.currentDate.toString())
                Text(
                    text = data.currentTemperature.toString() + "°",
                    fontSize = 36.sp
                )
            }
        }
    }
}

val data = WeatherData(
    currentDate = getCurrentDate(),
    domain = WeatherSourceDomain.Meta,
    url = "https://pogoda.meta.ua/ua/Chernihivska/Koropskyi/Sverdlovka/",
    currentSkyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Five)),
    currentTemperature = -25,
    weatherByDates = listOf(
        DayWeatherCondition(
            date = getCurrentDate(),
            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Three)),
            dayTemperature = -20,
            nightTemperature = -30,
            weatherByHours = listOf(
                WeatherSlice(
                    time = "06:00",
                    skyCondition = SkyCondition("4_s_5_d"),
                    precipitationProbability = 99
                ),
                WeatherSlice(
                    time = "09:00",
                    skyCondition = SkyCondition("4_s_3_d"),
                    precipitationProbability = 88
                ),
                WeatherSlice(
                    time = "12:00",
                    skyCondition = SkyCondition("4_s_2_d"),
                    precipitationProbability = 32
                ),
                WeatherSlice(
                    time = "15:00",
                    skyCondition = SkyCondition("4_c_0_d"),
                    precipitationProbability = 0
                )
            )
        )
    )
)

@Preview
@Composable
fun weatherDataPreview() {
    WeatherDataViewer(data = data)
}