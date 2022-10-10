package com.bogdan801.weatheraggregator.domain.repository

import com.bogdan801.weatheraggregator.data.remote.api.OpenWeatherApi
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.coroutines.CoroutineScope
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
    suspend fun deleteWeatherDataByDomain(domain: WeatherSourceDomain)

    //select
    fun getAllWeatherDataFromCache(): Flow<List<WeatherData>>
    fun getWeatherDataByDomain(domain: WeatherSourceDomain): Flow<WeatherData>

    //network
    suspend fun getWeatherDataFromNetwork(domain: WeatherSourceDomain, location: Location): WeatherData

    suspend fun getWeatherDataFromNetwork(domains: List<WeatherSourceDomain>, location: Location): List<WeatherData>

    fun getApi():OpenWeatherApi
}