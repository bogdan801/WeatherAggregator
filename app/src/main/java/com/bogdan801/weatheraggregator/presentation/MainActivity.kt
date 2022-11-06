package com.bogdan801.weatheraggregator.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.domain.usecase.GetAverageWeatherDataUseCase
import com.bogdan801.weatheraggregator.domain.usecase.GetWeatherDataUseCase
import com.bogdan801.weatheraggregator.domain.util.Resource
import com.bogdan801.weatheraggregator.presentation.composables.WeatherDataViewer
import com.bogdan801.weatheraggregator.presentation.theme.WeatherAggregatorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import javax.inject.Inject

data class WeatherDataState(
    val data: WeatherData = WeatherData(),
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

        val location = Location(metaLink = "/ua/Chernihivska/Koropskyi/Sverdlovka/", sinoptikLink = "//ua.sinoptik.ua/погода-деснянське","Деснянське")

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

        val averageState = mutableStateOf(
            WeatherDataState(
                WeatherData(domain = WeatherSourceDomain.Average)
            )
        )

        val useCase = GetWeatherDataUseCase(repo)
        val avgUseCase = GetAverageWeatherDataUseCase()

        var jobs = updateData(useCase, avgUseCase, dataStateList, averageState, location)

        setContent {
            WeatherAggregatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Box(modifier = Modifier.fillMaxSize()){
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                        ) {
                            dataStateList.forEach { dataState ->
                                WeatherDataViewer(
                                    modifier = Modifier.fillMaxWidth(),
                                    data = dataState.value.data,
                                    isLoading = dataState.value.isLoading,
                                    error = dataState.value.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            WeatherDataViewer(
                                modifier = Modifier.fillMaxWidth(),
                                data = averageState.value.data,
                                isLoading = averageState.value.isLoading,
                                error = averageState.value.error
                            )
                            Spacer(modifier = Modifier.height(80.dp))
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

                                    jobs = updateData(useCase, avgUseCase, dataStateList, averageState, location)
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

    private fun updateData(useCase: GetWeatherDataUseCase, avgUseCase: GetAverageWeatherDataUseCase, dataStateList: List<MutableState<WeatherDataState>>, avgState: MutableState<WeatherDataState>, location: Location): List<Job> {
        val jobs = mutableListOf<Job>()

        dataStateList.forEach { dataState ->
            val job = lifecycleScope.launch(Dispatchers.IO) {
                useCase(location, dataState.value.data.domain).cancellable().collect{ resource ->
                    when(resource){
                        is Resource.Loading -> {
                            if(resource.data != null){
                                dataState.value = WeatherDataState(
                                    data = resource.data,
                                    isLoading = false
                                )
                            }
                            else{
                                dataState.value = WeatherDataState(
                                    data = resource.data ?: WeatherData(),
                                    isLoading = true
                                )
                            }
                            avgState.value = WeatherDataState(isLoading = true)
                        }
                        is Resource.Error -> {
                            if(resource.e is NoConnectionException){
                                dataState.value = WeatherDataState(
                                    data = resource.data ?: WeatherData(),
                                    isLoading = false
                                )
                                this@MainActivity.runOnUiThread{
                                    Toast.makeText(this@MainActivity, resource.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                dataState.value = WeatherDataState(
                                    data = resource.data ?: WeatherData(),
                                    isLoading = false,
                                    error = resource.message
                                )
                            }
                            avgState.value = WeatherDataState(error = "Виникла помилка")
                        }
                        is Resource.Success -> {
                            dataState.value = WeatherDataState(
                                data = resource.data ?: WeatherData(),
                                isLoading = false
                            )
                            avgState.value = WeatherDataState(
                                data = avgUseCase(dataStateList.map { it.value.data }, List(dataStateList.size){ 1.0/dataStateList.size }),
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
