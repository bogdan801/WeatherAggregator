package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
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

        var jobs = updateData(useCase, dataStateList, location)

        setContent {
            WeatherAggregatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {

                    Box(modifier = Modifier.fillMaxSize()){
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
                            item{
                                Spacer(modifier = Modifier.height(64.dp))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    jobs.forEach { it.cancel() }

                                    jobs = updateData(useCase, dataStateList, location)
                                }
                            ) {
                                Text(text = "Оновити")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateData(useCase: GetWeatherDataUseCase, dataStateList: List<MutableState<WeatherDataState>>, location: Location): List<Job> {
        val jobs = mutableListOf<Job>()

        dataStateList.forEach { dataState ->
            val job = lifecycleScope.launch(Dispatchers.IO) {
                useCase(location, dataState.value.data.domain).cancellable().collect{ resource ->
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

            jobs.add(job)
        }

        return jobs
    }
}
