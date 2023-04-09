package com.bogdan801.weatheraggregator.data.localdb

import androidx.room.*
import androidx.room.Dao
import com.bogdan801.weatheraggregator.data.localdb.entities.DayWeatherEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.LocationEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherDataEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherSliceEntity
import com.bogdan801.weatheraggregator.data.localdb.relations.DataWithDaysJunction
import com.bogdan801.weatheraggregator.data.localdb.relations.DayWithSlicesJunction
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    //insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherDataEntity(weatherDataEntity: WeatherDataEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayWeatherEntity(dayWeatherEntity: DayWeatherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherSliceEntity(weatherSliceEntity: WeatherSliceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationEntity(locationEntity: LocationEntity)

    //delete
    @Query("DELETE FROM weathersliceentity")
    suspend fun deleteAllWeatherSliceEntities()

    @Query("DELETE FROM dayweatherentity")
    suspend fun deleteAllDayWeatherEntities()

    @Query("DELETE FROM weatherdataentity")
    suspend fun deleteAllWeatherDataEntities()

    @Query("DELETE FROM weathersliceentity WHERE sliceID == :sliceID")
    suspend fun deleteWeatherSliceEntity(sliceID: Int)

    @Query("DELETE FROM weathersliceentity WHERE dayID == :dayID AND time == :time")
    suspend fun deleteWeatherSliceEntityByDayIDAndTime(dayID: Int, time: String)

    @Query("DELETE FROM dayweatherentity WHERE dayID == :dayID")
    suspend fun deleteDayWeatherEntity(dayID: Int)

    @Query("DELETE FROM dayweatherentity WHERE date == :date AND dataID == :dataID")
    suspend fun deleteDayWeatherEntityByDateAndDataID(date: String, dataID: Int)


    @Query("DELETE FROM weatherdataentity WHERE dataID == :dataID")
    suspend fun deleteWeatherDataEntity(dataID: Int)

    @Query("DELETE FROM weatherdataentity WHERE domain == :domain")
    suspend fun deleteWeatherDataEntityByDomain(domain: Int)

    //select
    @Query("SELECT * FROM weatherdataentity")
    fun getAllWeatherDataEntities(): Flow<List<WeatherDataEntity>>

    @Query("SELECT * FROM dayweatherentity WHERE dataID == :dataID")
    fun getAllDayWeatherEntitiesForAGivenDataID(dataID: Int): Flow<List<DayWeatherEntity>>

    @Query("SELECT * FROM weathersliceentity  WHERE dayID == :dayID")
    suspend fun getAllSliceEntitiesForAGivenDayID(dayID: Int): List<WeatherSliceEntity>

    @Transaction
    @Query("SELECT * FROM dayweatherentity WHERE dataID == :dataID")
    fun getDayEntitiesWithSliceEntitiesByDataID(dataID: Int): Flow<List<DayWithSlicesJunction>>

    @Transaction
    @Query("SELECT * FROM weatherdataentity")
    fun getWeatherDataEntitiesWithDayEntities(): Flow<List<DataWithDaysJunction>>

    @Transaction
    @Query("SELECT * FROM weatherdataentity WHERE domain == :domain")
    fun getWeatherDataEntityByDomain(domain: Int): Flow<DataWithDaysJunction?>

    @Query("SELECT DISTINCT oblastName FROM locationentity")
    suspend fun getOblastList(): List<String>

    @Query("SELECT DISTINCT regionName FROM locationentity WHERE oblastName == :oblastName")
    suspend fun getOblastRegionList(oblastName: String): List<String>

    @Query("SELECT DISTINCT name FROM locationentity WHERE oblastName == :oblastName AND regionName == :regionName")
    suspend fun getLocationsList(oblastName: String, regionName: String): List<String>

    @Query("SELECT * FROM locationentity WHERE oblastName == :oblastName AND regionName == :regionName AND name == :townName")
    suspend fun getLocationEntity(oblastName: String, regionName: String, townName: String): List<LocationEntity>

    @Query("SELECT DISTINCT oblastName FROM LocationEntity WHERE oblastName LIKE '%' || :prompt || '%'")
    suspend fun searchOblasts(prompt: String): List<String>

    @Query("SELECT * FROM LocationEntity le " +
            "INNER JOIN (" +
            "  SELECT DISTINCT regionName, oblastName, MIN(locationID) AS locationID " +
            "  FROM LocationEntity " +
            "  WHERE regionName LIKE '%' || :prompt || '%' " +
            "  GROUP BY regionName, oblastName " +
            ") subquery ON le.locationID = subquery.locationID")
    suspend fun searchRegions(prompt: String): List<LocationEntity>

    @Query("SELECT * FROM LocationEntity WHERE name LIKE '%' || :prompt || '%' ORDER BY LENGTH(name) ASC")
    suspend fun searchLocations(prompt: String): List<LocationEntity>
}