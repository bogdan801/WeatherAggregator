package com.bogdan801.weatheraggregator.presentation.composables

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.bogdan801.weatheraggregator.domain.model.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.data.util.toDegree
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

@Composable
fun WeatherDataViewer(
    modifier: Modifier = Modifier,
    data: WeatherData
){
    var currentSkyCondition by remember{ mutableStateOf(data.currentSkyCondition)}
    var date by remember{ mutableStateOf(data.currentDate)}
    var currentTemperature by remember{ mutableStateOf(data.currentTemperature)}
    var selectedDayIndex by remember{ mutableStateOf(0)}
    if(data.weatherByDates.isNotEmpty()){
        Column(modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(96.dp),
                    painter = currentSkyCondition.getPainterResource(),
                    contentDescription = ""
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "${data.currentLocation} - $date")
                    Text(
                        text = currentTemperature.toDegree(),
                        fontSize = 36.sp
                    )
                }
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                val spacing = 2.dp
                Column(modifier = Modifier
                    .width(60.dp)
                ) {
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Час",         fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Хмарність",   fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Температура", fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Опади",       fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Тиск",        fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Вологість",   fontSize = 10.sp)
                    Text(modifier = Modifier.padding(vertical = spacing), text = "Вітер",       fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                data.weatherByDates[selectedDayIndex].weatherByHours.forEachIndexed { index, slice->
                    Column(modifier = Modifier
                        .weight(1f)
                        .background(if (index % 2 == 0) Color(0xFFF3FAFD) else Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.time, fontSize = 10.sp)
                        Image(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(16.dp),
                            painter = slice.skyCondition.getPainterResource(),
                            contentDescription = ""
                        )
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.temperature.toString(), fontSize = 10.sp)
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.precipitationProbability.toString(), fontSize = 10.sp)
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.pressure.toString(), fontSize = 10.sp)
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.humidity.toString(), fontSize = 10.sp)
                        Text(modifier = Modifier.padding(vertical = spacing), text = slice.wind.toString(), fontSize = 8.sp)
                    }
                }
            }

            LazyVerticalGrid(
                modifier = Modifier.padding(vertical = 8.dp),
                columns = GridCells.Fixed(data.weatherByDates.size)
            ){
                itemsIndexed(data.weatherByDates){ index, day ->
                    Box(modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            if (selectedDayIndex == index)
                                Color(0xFFE3ECF7)
                            else Color(0xFFF1F7FA)
                        )
                        .clickable {
                            selectedDayIndex = index
                            if (index == 0) {
                                currentSkyCondition = data.currentSkyCondition
                                currentTemperature = data.currentTemperature
                                date = data.currentDate
                            } else {
                                currentSkyCondition = day.skyCondition
                                currentTemperature = day.dayTemperature
                                date = day.date
                            }
                        }
                    ){
                        Image(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            painter = day.skyCondition.getPainterResource(),
                            contentDescription = ""
                        )
                        Text(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(4.dp),
                            text = day.date.toString(),
                            fontSize = 10.sp
                        )
                        Text(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(4.dp),
                            text = day.nightTemperature.toDegree() + "  " + day.dayTemperature.toDegree(),
                            fontSize = 10.sp
                        )
                    }
                }
            }

        }
    }


}

/*

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
        ),
        DayWeatherCondition(
            date = getCurrentDate() + DatePeriod(days = 1),
            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Three)),
            dayTemperature = -25,
            nightTemperature = -32,
            weatherByHours = listOf(
                WeatherSlice(
                    time = "06:00",
                    skyCondition = SkyCondition("4_s_5_d"),
                    precipitationProbability = 99
                ),
                WeatherSlice(
                    time = "09:00",
                    skyCondition = SkyCondition("4_s_5_d"),
                    precipitationProbability = 88
                ),
                WeatherSlice(
                    time = "12:00",
                    skyCondition = SkyCondition("4_s_4_d"),
                    precipitationProbability = 32
                ),
                WeatherSlice(
                    time = "15:00",
                    skyCondition = SkyCondition("4_s_3_d"),
                    precipitationProbability = 0
                )
            )
        ),
        DayWeatherCondition(
            date = getCurrentDate() + DatePeriod(days = 2),
            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Three)),
            dayTemperature = -12,
            nightTemperature = -15,
            weatherByHours = listOf(
                WeatherSlice(
                    time = "06:00",
                    skyCondition = SkyCondition("4_s_2_d"),
                    precipitationProbability = 99
                ),
                WeatherSlice(
                    time = "09:00",
                    skyCondition = SkyCondition("4_s_2_d"),
                    precipitationProbability = 88
                ),
                WeatherSlice(
                    time = "12:00",
                    skyCondition = SkyCondition("4_s_1_d"),
                    precipitationProbability = 32
                ),
                WeatherSlice(
                    time = "15:00",
                    skyCondition = SkyCondition("4_s_4_d"),
                    precipitationProbability = 0
                )
            )
        ),
        DayWeatherCondition(
            date = getCurrentDate() + DatePeriod(days = 3),
            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Three)),
            dayTemperature = -14,
            nightTemperature = -20,
            weatherByHours = listOf(
                WeatherSlice(
                    time = "06:00",
                    skyCondition = SkyCondition("4_s_3_d"),
                    precipitationProbability = 99
                ),
                WeatherSlice(
                    time = "09:00",
                    skyCondition = SkyCondition("4_s_2_d"),
                    precipitationProbability = 88
                ),
                WeatherSlice(
                    time = "12:00",
                    skyCondition = SkyCondition("4_s_3_d"),
                    precipitationProbability = 32
                ),
                WeatherSlice(
                    time = "15:00",
                    skyCondition = SkyCondition("4_s_1_d"),
                    precipitationProbability = 0
                )
            )
        ),
        DayWeatherCondition(
            date = getCurrentDate() + DatePeriod(days = 4),
            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Three)),
            dayTemperature = -14,
            nightTemperature = -23,
            weatherByHours = listOf(
                WeatherSlice(
                    time = "06:00",
                    skyCondition = SkyCondition("4_s_2_d"),
                    precipitationProbability = 99
                ),
                WeatherSlice(
                    time = "09:00",
                    skyCondition = SkyCondition("4_s_3_d"),
                    precipitationProbability = 88
                ),
                WeatherSlice(
                    time = "12:00",
                    skyCondition = SkyCondition("4_s_1_d"),
                    precipitationProbability = 32
                ),
                WeatherSlice(
                    time = "15:00",
                    skyCondition = SkyCondition("4_s_2_d"),
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
}*/
