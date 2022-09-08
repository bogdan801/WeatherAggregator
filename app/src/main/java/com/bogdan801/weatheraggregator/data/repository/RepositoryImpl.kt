package com.bogdan801.weatheraggregator.data.repository

import com.bogdan801.weatheraggregator.data.localdb.Dao
import com.bogdan801.weatheraggregator.data.mapper.toDayWeatherEntity
import com.bogdan801.weatheraggregator.data.mapper.toWeatherData
import com.bogdan801.weatheraggregator.data.mapper.toWeatherDataEntity
import com.bogdan801.weatheraggregator.data.mapper.toWeatherSliceEntity
import com.bogdan801.weatheraggregator.domain.model.DayWeatherCondition
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSlice
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import com.bogdan801.weatheraggregator.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RepositoryImpl(private val dao: Dao) : Repository {
    //INSERT
    override suspend fun insertWeatherData(weatherData: WeatherData) {
        dao.deleteWeatherDataEntityByDomain(weatherData.domain.ordinal)

        dao.insertWeatherDataEntity(weatherData.toWeatherDataEntity())
        weatherData.weatherByDates.forEach { day ->
            dao.insertDayWeatherEntity(day.toDayWeatherEntity())
            day.weatherByHours.forEach { slice ->
                dao.insertWeatherSliceEntity(slice.toWeatherSliceEntity())
            }
        }
    }

    override suspend fun insertDayWeather(dayWeatherCondition: DayWeatherCondition) {
        dao.deleteDayWeatherEntityByDateAndDataID(dayWeatherCondition.date.toString(), dayWeatherCondition.dataID)

        dao.insertDayWeatherEntity(dayWeatherCondition.toDayWeatherEntity())
        dayWeatherCondition.weatherByHours.forEach { slice ->
            dao.insertWeatherSliceEntity(slice.toWeatherSliceEntity())
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
    override fun getAllWeatherDataFromCache(): Flow<List<WeatherData>> {

        return flow {  }
    }


    //NETWORK
    override suspend fun getWeatherDataFromNetwork(
        domain: WeatherSourceDomain,
        location: String
    ): WeatherData {
        TODO("Not yet implemented")
    }
}