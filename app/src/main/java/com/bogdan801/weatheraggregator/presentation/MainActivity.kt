package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.bogdan801.weatheraggregator.data.localdb.Dao
import com.bogdan801.weatheraggregator.data.remote.parsing.getWeatherDataFromMeta
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val data = WeatherData(
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

        lateinit var dataFromDB: List<WeatherData>

        runBlocking{
            //repo.insertWeatherData(data)
            //repo.deleteAllWeatherData()
            dataFromDB = repo.getAllWeatherDataFromCache().first()
        }*/

        //val oblastList = getOblastListFromFile(this)

        lifecycleScope.launch(Dispatchers.Default) {
            getWeatherDataFromMeta(Location(link = "/ua/Chernihivska/Koropskyi/Sverdlovka/", "Деснянське"))
        }

        println()
        setContent {
            WeatherAggregatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    /*LazyColumn(Modifier.fillMaxSize().padding(horizontal = 8.dp)){
                        items(oblastList[24].listOfRegions[7].locations.map { it.name }){ name ->
                            Text(text = name)
                        }
                    }*/
                }
            }
        }
    }
}
