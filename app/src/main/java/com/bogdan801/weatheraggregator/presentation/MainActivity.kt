package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.composables.WeatherDataViewer
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val oblastList = getOblastListFromFile(this)

        val location = Location(link = "/ua/Chernihivska/Koropskyi/Sverdlovka/", "Деснянське")

        val dataList = mutableStateOf<List<WeatherData>>(listOf())

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val list = repo.getWeatherDataFromNetwork(listOf(WeatherSourceDomain.Meta, WeatherSourceDomain.Sinoptik, WeatherSourceDomain.OpenWeather), location)

                list.forEach { data ->
                    repo.insertWeatherData(data)
                }
            }
            catch (e: Exception){
                println(e)
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            repo.getAllWeatherDataFromCache().collect{ data ->
                dataList.value = data
            }
        }
        
        setContent {
            WeatherAggregatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(dataList.value){ item ->
                            WeatherDataViewer(
                                modifier = Modifier.fillMaxWidth(),
                                data = item
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
