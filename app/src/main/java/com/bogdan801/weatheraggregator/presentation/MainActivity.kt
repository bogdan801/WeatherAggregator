package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.bogdan801.weatheraggregator.presentation.navigation.Navigation
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var theme: MutableState<Theme>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*val day = DayWeatherCondition(
            date = getCurrentDate(),
            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(
                SnowLevel.Three)),
            dayTemperature = -20,
            nightTemperature = -30,
            weatherByHours = listOf(
                WeatherSlice(
                    time = "00:00",
                    temperature = -5,
                    skyCondition = SkyCondition("3_s_1_n"),
                    precipitationProbability = 74,
                    pressure = 756,
                    humidity = 78,
                    wind = Wind.create(0, 5)
                ),
                WeatherSlice(
                    time = "03:00",
                    temperature = -7,
                    skyCondition = SkyCondition("2_s_3_n"),
                    precipitationProbability = 80,
                    pressure = 758,
                    humidity = 80,
                    wind = Wind.create(1, 4)
                ),
                WeatherSlice(
                    time = "06:00",
                    temperature = -8,
                    skyCondition = SkyCondition("2_s_5_n"),
                    precipitationProbability = 99,
                    pressure = 760,
                    humidity = 85,
                    wind = Wind.create(7, 3)
                ),
                WeatherSlice(
                    time = "09:00",
                    temperature = -6,
                    skyCondition = SkyCondition("2_s_3_d"),
                    precipitationProbability = 88,
                    pressure = 762,
                    humidity = 90,
                    wind = Wind.create(7, 4)
                ),
                WeatherSlice(
                    time = "12:00",
                    temperature = -5,
                    skyCondition = SkyCondition("2_s_2_d"),
                    precipitationProbability = 32,
                    pressure = 763,
                    humidity = 94,
                    wind = Wind.create(7, 2)
                ),
                WeatherSlice(
                    time = "15:00",
                    temperature = -4,
                    skyCondition = SkyCondition("2_c_0_d"),
                    precipitationProbability = 0,
                    pressure = 764,
                    humidity = 95,
                    wind = Wind.create(7, 2)
                ),
                WeatherSlice(
                    time = "18:00",
                    temperature = -5,
                    skyCondition = SkyCondition("2_s_1_n"),
                    precipitationProbability = 13,
                    pressure = 762,
                    humidity = 95,
                    wind = Wind.create(7, 3)
                ),
                WeatherSlice(
                    time = "21:00",
                    temperature = -7,
                    skyCondition = SkyCondition("2_s_2_n"),
                    precipitationProbability = 33,
                    pressure = 761,
                    humidity = 97,
                    wind = Wind.create(6, 2)
                )
            )
        )*/

        setContent {
            WeatherAggregatorTheme(theme.value) {
                //setting up system bars color
                val statusBar = when(theme.value){
                    Theme.Auto -> if(isSystemInDarkTheme()) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.secondary
                    Theme.Light -> MaterialTheme.colors.secondary
                    Theme.Dark -> MaterialTheme.colors.primaryVariant
                }
                val systemUiController = rememberSystemUiController()
                systemUiController.setStatusBarColor(statusBar)
                systemUiController.setNavigationBarColor(MaterialTheme.colors.secondary)

                //screens and navigation
                Navigation(navController = rememberNavController())
            }
        }
    }
}
