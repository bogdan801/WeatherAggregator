package com.bogdan801.weatheraggregator.presentation.screens.home

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogdan801.weatheraggregator.data.datastore.saveIntToDataStore
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel
@Inject
constructor(
    private val repository: Repository,
    private val theme: MutableState<Theme>,
    handle: SavedStateHandle
): ViewModel() {
    //theme handling
    val themeState: State<Theme> = theme
    fun setTheme(ordinal: Int, context: Context){
        theme.value = Theme.values()[ordinal]
        viewModelScope.launch {
            context.saveIntToDataStore("THEME", ordinal)
        }
    }

    //loading
    private val _isLoadingState = mutableStateOf(false)
    val isLoadingState: State<Boolean> = _isLoadingState
    fun updateWeatherData(){
        viewModelScope.launch {
            _isLoadingState.value = true
            delay(3000)
            _isLoadingState.value = false
        }
    }

    //day selection
    private val _selectedDayState = mutableStateOf(0)
    val selectedDayState: State<Int> = _selectedDayState
    val selectedDay get() = currentData.weatherByDates[_selectedDayState.value]
    fun setSelectedDay(index: Int, slideRight: (Boolean) -> Unit = {}){
        if(selectedDayState.value > index){
            slideRight(true)
            _selectedDayState.value = index
        }
        else if(selectedDayState.value < index){
            slideRight(false)
            _selectedDayState.value = index
        }
    }


    //data selection
    private val _dataListState = mutableStateOf(
        listOf(
            WeatherDataState(
                data = WeatherData(
                    currentDate = getCurrentDate(),
                    domain = WeatherSourceDomain.Average,
                    url = "",
                    currentSkyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Five)),
                    currentTemperature = -25,
                    weatherByDates = listOf(
                        DayWeatherCondition(
                            date = getCurrentDate(),
                            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(
                                SnowLevel.One)),
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
                        ),
                        DayWeatherCondition(
                            date = getCurrentDate() + DatePeriod(days = 5),
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
                            date = getCurrentDate() + DatePeriod(days = 6),
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
            ),
            WeatherDataState(
                data = WeatherData(
                    currentDate = getCurrentDate(),
                    domain = WeatherSourceDomain.Meta,
                    url = "",
                    currentSkyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Five)),
                    currentTemperature = -25,
                    weatherByDates = listOf(
                        DayWeatherCondition(
                            date = getCurrentDate(),
                            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(
                                SnowLevel.One)),
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
                        ),
                        DayWeatherCondition(
                            date = getCurrentDate() + DatePeriod(days = 5),
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
                            date = getCurrentDate() + DatePeriod(days = 6),
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
            ),
            WeatherDataState(
                data = WeatherData(domain = WeatherSourceDomain.Sinoptik),
                error = "ohhh no that's so sad"
            ),
            WeatherDataState(
                data = WeatherData(
                    currentDate = getCurrentDate(),
                    domain = WeatherSourceDomain.OpenWeather,
                    url = "",
                    currentSkyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(SnowLevel.Five)),
                    currentTemperature = -25,
                    weatherByDates = listOf(
                        DayWeatherCondition(
                            date = getCurrentDate(),
                            skyCondition = SkyCondition(_cloudiness = Cloudiness.Gloomy, _precipitation = Precipitation.Snow(
                                SnowLevel.One)),
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
                        ),
                        DayWeatherCondition(
                            date = getCurrentDate() + DatePeriod(days = 5),
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
                            date = getCurrentDate() + DatePeriod(days = 6),
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
            )
        )
    )
    val dataListState: State<List<WeatherDataState>> = _dataListState
    private val _selectedDataIndexState = mutableStateOf(0)
    val selectedDataIndexState: State<Int> = _selectedDataIndexState
    val currentData get() = _dataListState.value[_selectedDataIndexState.value].data
    fun setSelectedData(index: Int){
        setSelectedDay(0)
        _selectedDataIndexState.value = index
    }
}