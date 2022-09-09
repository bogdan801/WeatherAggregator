package com.bogdan801.weatheraggregator.data.repository

import com.bogdan801.weatheraggregator.data.localdb.Dao
import com.bogdan801.weatheraggregator.data.localdb.relations.DataWithDaysJunction
import com.bogdan801.weatheraggregator.data.mapper.*
import com.bogdan801.weatheraggregator.domain.model.DayWeatherCondition
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import com.bogdan801.weatheraggregator.domain.repository.Repository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class RepositoryImpl(private val dao: Dao) : Repository {
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


    //NETWORK
    override suspend fun getWeatherDataFromNetwork(
        domain: WeatherSourceDomain,
        location: String
    ): WeatherData {
        TODO("Not yet implemented")
    }
}