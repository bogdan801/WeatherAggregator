package com.bogdan801.weatheraggregator.data.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bogdan801.weatheraggregator.data.localdb.entities.DayWeatherEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.LocationEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherDataEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherSliceEntity

@Database(
    entities = [WeatherSliceEntity::class, DayWeatherEntity::class, WeatherDataEntity::class, LocationEntity::class],
    version = 2
)
abstract class Database: RoomDatabase() {
    abstract val dbDao: Dao
}