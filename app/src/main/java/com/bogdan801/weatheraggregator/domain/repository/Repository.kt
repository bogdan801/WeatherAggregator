package com.bogdan801.weatheraggregator.domain.repository

import com.bogdan801.weatheraggregator.data.remote.api.OpenWeatherApi
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.coroutines.flow.Flow

interface Repository {
    //insert
    suspend fun insertWeatherData(weatherData: WeatherData)
    suspend fun insertDayWeather(dayWeatherCondition: DayWeatherCondition)
    suspend fun insertWeatherSlice(weatherSlice: WeatherSlice)
    suspend fun insertLocation(location: Location)

    //delete
    suspend fun deleteWeatherSlice(sliceID: Int)
    suspend fun deleteDayWeatherCondition(dayID: Int)
    suspend fun deleteWeatherData(dataID: Int)
    suspend fun deleteAllWeatherData()
    suspend fun deleteWeatherDataByDomain(domain: WeatherSourceDomain)

    //select
    fun getAllWeatherDataFromCache(): Flow<List<WeatherData>>
    fun getWeatherDataByDomain(domain: WeatherSourceDomain): Flow<WeatherData>
    suspend fun getOblastList(): List<String>
    suspend fun getOblastRegionList(oblastName: String): List<String>
    suspend fun getLocationsList(oblastName: String, regionName: String): List<String>
    suspend fun getLocation(oblastName: String, regionName: String, townName: String): List<Location>
    suspend fun searchOblasts(prompt: String): List<Location>
    suspend fun searchRegions(prompt: String): List<Location>
    suspend fun searchLocations(prompt: String): List<Location>

    //network
    suspend fun getWeatherDataFromNetwork(domain: WeatherSourceDomain, location: Location): WeatherData

    suspend fun getWeatherDataFromNetwork(domains: List<WeatherSourceDomain>, location: Location): List<WeatherData>

    fun getApi():OpenWeatherApi
}