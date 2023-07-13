package com.bogdan801.weatheraggregator.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.bogdan801.weatheraggregator.BuildConfig
import com.bogdan801.weatheraggregator.data.localdb.Dao
import com.bogdan801.weatheraggregator.data.mapper.*
import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.api.OpenWeatherApi
import com.bogdan801.weatheraggregator.data.remote.parsing.meta.getWeatherDataFromMeta
import com.bogdan801.weatheraggregator.data.remote.parsing.sinoptik.getWeatherDataFromSinoptik
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.net.UnknownHostException

class RepositoryImpl(private val dao: Dao, private val openWeatherApi: OpenWeatherApi) : Repository {
    //INSERT
    override suspend fun insertWeatherData(weatherData: WeatherData) {
        dao.deleteWeatherDataEntityByDomain(weatherData.domain.ordinal)

        val dataID = dao.insertWeatherDataEntity(weatherData.toWeatherDataEntity()).toInt()
        weatherData.weatherByDates.forEach { day ->
            val dayID = dao.insertDayWeatherEntity(day.toDayWeatherEntity().copy(dataID = dataID)).toInt()
            day.weatherByHours.forEach { slice ->
                dao.insertWeatherSliceEntity(slice.toWeatherSliceEntity().copy(dayID = dayID))
            }
        }
    }

    override suspend fun insertDayWeather(dayWeatherCondition: DayWeatherCondition) {
        dao.deleteDayWeatherEntityByDateAndDataID(dayWeatherCondition.date.toString(), dayWeatherCondition.dataID)

        val dayID = dao.insertDayWeatherEntity(dayWeatherCondition.toDayWeatherEntity()).toInt()
        dayWeatherCondition.weatherByHours.forEach { slice ->
            dao.insertWeatherSliceEntity(slice.toWeatherSliceEntity().copy(dayID = dayID))
        }
    }

    override suspend fun insertWeatherSlice(weatherSlice: WeatherSlice) {
        dao.deleteWeatherSliceEntityByDayIDAndTime(weatherSlice.dayID, weatherSlice.time)
        dao.insertWeatherSliceEntity(weatherSlice.toWeatherSliceEntity())
    }

    override suspend fun insertLocation(location: Location) {
        dao.insertLocationEntity(location.toLocationEntity())
    }

    //DELETE
    override suspend fun deleteWeatherSlice(sliceID: Int) {
        dao.deleteWeatherSliceEntity(sliceID)
    }

    override suspend fun deleteDayWeatherCondition(dayID: Int) {
        dao.deleteDayWeatherEntity(dayID)
    }

    override suspend fun deleteWeatherData(dataID: Int) {
        dao.deleteWeatherDataEntity(dataID)
    }

    override suspend fun deleteAllWeatherData() {
        dao.deleteAllWeatherDataEntities()
    }

    override suspend fun deleteWeatherDataByDomain(domain: WeatherSourceDomain) {
        dao.deleteWeatherDataEntityByDomain(domain.ordinal)
    }

    //SELECT
    override fun getAllWeatherDataFromCache(): Flow<List<WeatherData>> = dao.getWeatherDataEntitiesWithDayEntities().map {
        it.map { junction ->
            junction.toWeatherData().apply {
                this.weatherByDates.forEach { day ->
                    day.weatherByHours = dao.getAllSliceEntitiesForAGivenDayID(day.dayID).map { entity -> entity.toWeatherSlice() }
                }
            }
        }
    }

    override suspend fun getWeatherDataByDomain(domain: WeatherSourceDomain): Flow<WeatherData> = dao.getWeatherDataEntityByDomain(domain.ordinal).map { junction ->
        junction?.toWeatherData()?.apply {
            this.weatherByDates.forEach { day ->
                day.weatherByHours = dao.getAllSliceEntitiesForAGivenDayID(day.dayID).map { entity -> entity.toWeatherSlice() }
            }
        } ?: WeatherData()
    }

    override suspend fun getCachedDomains() = dao.getAllWeatherDataEntities().first().map { WeatherSourceDomain.values()[it.domain] }.sortedBy { it.ordinal }

    override suspend fun getOblastList() = dao.getOblastList()

    override suspend fun getOblastRegionList(oblastName: String) = dao.getOblastRegionList(oblastName)

    override suspend fun getLocationsList(oblastName: String, regionName: String) = dao.getLocationsList(oblastName, regionName)

    override suspend fun getLocation(
        oblastName: String,
        regionName: String,
        townName: String
    ) = dao.getLocationEntity(oblastName, regionName, townName).map { it.toLocation() }

    override suspend fun searchOblasts(prompt: String): List<Location> =
        dao.searchOblasts(prompt).map {
            Location("", "", "", "", it, -1.0, -1.0)
        }

    override suspend fun searchRegions(prompt: String): List<Location> =
        dao.searchRegions(prompt).map { locationEntity ->
            Location("", "", "", locationEntity.regionName, locationEntity.oblastName, -1.0, -1.0)
        }

    override suspend fun searchLocations(prompt: String): List<Location> =
        dao.searchLocations(prompt).map { it.toLocation() }

    //NETWORK
    override suspend fun getWeatherDataFromNetwork(
        domain: WeatherSourceDomain,
        location: Location
    ): WeatherData = when(domain) {
        WeatherSourceDomain.Meta -> {
            getWeatherDataFromMeta(location)
        }
        WeatherSourceDomain.Sinoptik -> {
            getWeatherDataFromSinoptik(location)
        }
        WeatherSourceDomain.OpenWeather -> {
            try {
                val apiKey = BuildConfig.API_KEY
                openWeatherApi.getWeatherData(location.lat.toString(), location.lon.toString(), "metric", apiKey).toWeatherData(location)
            }
            catch (e: UnknownHostException){
                throw NoConnectionException("No internet connection")
            }
        }
        else -> WeatherData()
    }

    override fun getApi():OpenWeatherApi = openWeatherApi

    //LOCATION
    @SuppressLint("MissingPermission")
    override fun getDeviceLocation(context: Context, onLocationReceived: (location: android.location.Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }
        ).addOnSuccessListener(onLocationReceived)
    }

    override suspend fun getClosestLocation(latitude: Double, longitude: Double): Location?
        = dao.getClosestLocation(latitude, longitude)?.toLocation()
}

