package com.bogdan801.weatheraggregator.domain.repository

import com.bogdan801.weatheraggregator.data.remote.api.OpenWeatherApi
import com.bogdan801.weatheraggregator.domain.model.DayWeatherCondition
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import kotlinx.coroutines.flow.Flow

interface Repository {
    //insert
    suspend fun insertWeatherData(weatherData: WeatherData)
    suspend fun insertDayWeather(dayWeatherCondition: DayWeatherCondition)
    suspend fun insertWeatherSlice(weatherSlice: WeatherSlice)

    //delete
    suspend fun deleteWeatherSlice(sliceID: Int)
    suspend fun deleteDayWeatherCondition(dayID: Int)
    suspend fun deleteWeatherData(dataID: Int)
    suspend fun deleteAllWeatherData()

    //select
    fun getAllWeatherDataFromCache(): Flow<List<WeatherData>>

    //network
    suspend fun getWeatherDataFromNetwork(domain: WeatherSourceDomain, location: String): WeatherData

    fun getApi():OpenWeatherApi
}