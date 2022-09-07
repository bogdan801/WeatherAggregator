package com.bogdan801.weatheraggregator.domain.repository

import com.bogdan801.weatheraggregator.domain.model.DayWeatherCondition
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import kotlinx.coroutines.flow.Flow

interface Repository {
    //insert
    suspend fun insertWeatherData(weatherData: WeatherData)
    suspend fun insertDayWeather(dayWeatherCondition: DayWeatherCondition)
    suspend fun insertWeatherSlice(weatherSlice: WeatherSlice)

    //delete
    suspend fun deleteWeatherSlice(sliceID: Int)
    suspend fun deleteAllWeatherSlicesByDayID(dayID: Int)
    suspend fun deleteAllWeatherSlices()

    suspend fun deleteDayWeatherCondition(dayID: Int)
    suspend fun deleteDayWeatherConditionsByData(dataID: Int)
    suspend fun deleteAllDayWeatherConditions()

    suspend fun deleteWeatherData(dataID: Int)
    suspend fun deleteAllWeatherData()

    //select
    fun getWeatherData(): Flow<List<WeatherData>>
}