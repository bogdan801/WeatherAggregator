package com.bogdan801.weatheraggregator.domain.repository

import android.content.Context
import com.bogdan801.weatheraggregator.data.localdb.entities.LocationEntity
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
    suspend fun getWeatherDataByDomain(domain: WeatherSourceDomain): Flow<WeatherData>

    suspend fun getCachedDomains(): List<WeatherSourceDomain>

    suspend fun getOblastList(): List<String>
    suspend fun getOblastRegionList(oblastName: String): List<String>
    suspend fun getLocationsList(oblastName: String, regionName: String): List<String>
    suspend fun getLocation(oblastName: String, regionName: String, townName: String): List<Location>
    suspend fun searchOblasts(prompt: String): List<Location>
    suspend fun searchRegions(prompt: String): List<Location>
    suspend fun searchLocations(prompt: String): List<Location>

    //network
    suspend fun getWeatherDataFromNetwork(domain: WeatherSourceDomain, location: Location): WeatherData
    fun getApi():OpenWeatherApi

    //location
    fun getDeviceLocation(context: Context, onLocationReceived: (location: android.location.Location?) -> Unit)
    suspend fun getClosestLocation(latitude: Double, longitude: Double): Location?
}