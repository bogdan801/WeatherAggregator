package com.bogdan801.weatheraggregator.data.localdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherDataEntity(
    @PrimaryKey(autoGenerate = true)
    val dataID: Int,
    val currentDate: String,
    val currentLocation: String,
    val domain: Int,
    val url: String,
    val currentSkyCondition: String,
    val currentTemperature: Int
)
