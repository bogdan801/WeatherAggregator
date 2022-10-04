package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.bogdan801.weatheraggregator.BuildConfig
import com.bogdan801.weatheraggregator.data.remote.parsing.meta.getWeatherDataFromMeta
import com.bogdan801.weatheraggregator.data.remote.parsing.sinoptik.getWeatherDataFromSinoptik
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.composables.WeatherDataViewer
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val oblastList = getOblastListFromFile(this)

        val location = Location(link = "/ua/Chernihivska/Koropskyi/Sverdlovka/", "Деснянське")
        val apiKey = BuildConfig.API_KEY

        val metaDataState = mutableStateOf(WeatherData())
        val sinoptikDataState = mutableStateOf(WeatherData())
        val openWeatherDataState = mutableStateOf(WeatherData())

        lifecycleScope.launch(Dispatchers.Default) {
            val elapsed = measureTimeMillis {
                metaDataState.value = getWeatherDataFromMeta(location)
            }

            Log.d("puk", "Meta: $elapsed")
        }

        lifecycleScope.launch(Dispatchers.Default) {
            val elapsed = measureTimeMillis {
                sinoptikDataState.value = getWeatherDataFromSinoptik(location.toSinoptikLocation())
            }

            Log.d("puk", "Sinoptik: $elapsed")
        }

        lifecycleScope.launch(Dispatchers.Default){
            val elapsed = measureTimeMillis {
                val api = repo.getApi()
                val locInfo = api.getLocationInfo(location.name + ",ua", apiKey)[0]
                val data = api.getWeatherData(locInfo.lat.toString(), locInfo.lon.toString(), "metric", apiKey)

                openWeatherDataState.value = data.toWeatherData(location)
            }
            Log.d("puk", "OpenWeather: $elapsed")

        }

        setContent {
            WeatherAggregatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            WeatherDataViewer(
                                modifier = Modifier.fillMaxSize(),
                                data = metaDataState.value
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            WeatherDataViewer(
                                modifier = Modifier.fillMaxSize(),
                                data = sinoptikDataState.value
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            WeatherDataViewer(
                                modifier = Modifier.fillMaxSize(),
                                data = openWeatherDataState.value
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
