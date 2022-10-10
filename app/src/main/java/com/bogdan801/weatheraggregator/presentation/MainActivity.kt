package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.magnifier
import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.domain.usecase.GetWeatherDataUseCase
import com.bogdan801.weatheraggregator.domain.util.Resource
import com.bogdan801.weatheraggregator.presentation.composables.WeatherDataViewer
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherDataState(
    val data: WeatherData,
    val isLoading: Boolean = true,
    val error: String? = null
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val oblastList = getOblastListFromFile(this)

        val location = Location(link = "/ua/Chernihivska/Koropskyi/Sverdlovka/", "Деснянське")

        val dataStateList = listOf(
            mutableStateOf(
                WeatherDataState(
                    WeatherData(domain = WeatherSourceDomain.Meta)
                )
            ),
            mutableStateOf(
                WeatherDataState(
                    WeatherData(domain = WeatherSourceDomain.Sinoptik)
                )
            ),
            mutableStateOf(
                WeatherDataState(
                    WeatherData(domain = WeatherSourceDomain.OpenWeather)
                )
            )
        )

        val useCase = GetWeatherDataUseCase(repo)

        dataStateList.forEach { dataState ->
            lifecycleScope.launch(Dispatchers.IO) {
                useCase(location, dataState.value.data.domain).collect{ resource ->
                    when(resource){
                        is Resource.Loading -> {
                            dataState.value = WeatherDataState(
                                data = resource.data ?: WeatherData(),
                                isLoading = true
                            )
                        }
                        is Resource.Error -> {
                            dataState.value = WeatherDataState(
                                data = resource.data ?: WeatherData(),
                                isLoading = false,
                                error = resource.message
                            )
                        }
                        is Resource.Success -> {
                            dataState.value = WeatherDataState(
                                data = resource.data ?: WeatherData(),
                                isLoading = false
                            )
                        }
                    }
                }
            }

        }

        
        setContent {
            WeatherAggregatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(dataStateList){ dataState ->
                            WeatherDataViewer(
                                modifier = Modifier.fillMaxWidth(),
                                data = dataState.value.data,
                                isLoading = dataState.value.isLoading,
                                error = dataState.value.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
