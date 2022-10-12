package com.bogdan801.weatheraggregator.data.repository

import com.bogdan801.weatheraggregator.BuildConfig
import com.bogdan801.weatheraggregator.data.localdb.Dao
import com.bogdan801.weatheraggregator.data.mapper.*
import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.data.remote.api.OpenWeatherApi
import com.bogdan801.weatheraggregator.data.remote.parsing.meta.getWeatherDataFromMeta
import com.bogdan801.weatheraggregator.data.remote.parsing.sinoptik.getWeatherDataFromSinoptik
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.net.UnknownHostException
import kotlin.coroutines.coroutineContext

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

    override fun getWeatherDataByDomain(domain: WeatherSourceDomain): Flow<WeatherData> = dao.getWeatherDataEntityByDomain(domain.ordinal).map { junction ->
        junction?.toWeatherData()?.apply {
            this.weatherByDates.forEach { day ->
                day.weatherByHours = dao.getAllSliceEntitiesForAGivenDayID(day.dayID).map { entity -> entity.toWeatherSlice() }
            }
        } ?: WeatherData()
    }

    //NETWORK
    override suspend fun getWeatherDataFromNetwork(
        domain: WeatherSourceDomain,
        location: Location
    ): WeatherData = when(domain) {
        WeatherSourceDomain.Meta -> {
            getWeatherDataFromMeta(location)
        }
        WeatherSourceDomain.Sinoptik -> {
            getWeatherDataFromSinoptik(location.toSinoptikLocation())
        }
        WeatherSourceDomain.OpenWeather -> {
            try {
                val apiKey = BuildConfig.API_KEY
                val locInfo = openWeatherApi.getLocationInfo(location.name + ",ua", apiKey).let {
                    if(it.isEmpty()) throw WrongUrlException("Wrong URL. Location: ${location.name} doesn't exist")
                    it[0]
                }
                openWeatherApi.getWeatherData(locInfo.lat.toString(), locInfo.lon.toString(), "metric", apiKey).toWeatherData(location)
            }
            catch (e: UnknownHostException){
                throw NoConnectionException("No internet connection")
            }
        }
    }

    override suspend fun getWeatherDataFromNetwork(domains: List<WeatherSourceDomain>, location: Location): List<WeatherData>{
        var output = listOf<WeatherData>()

        coroutineScope {
            val list = mutableListOf<Deferred<WeatherData>>()
            domains.forEach { domain ->
                val data = async {
                    getWeatherDataFromNetwork(domain, location)
                }
                list.add(data)
            }

            output = list.map { it.await() }
        }

        return output
    }

    override fun getApi():OpenWeatherApi = openWeatherApi
}

