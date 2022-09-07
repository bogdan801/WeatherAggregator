package com.bogdan801.weatheraggregator.data.localdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeatherSliceEntity(
    @PrimaryKey(autoGenerate = true)
    val sliceID: Int,
    val dayID: Int,
    val time: String,
    val skyCondition: String,
    val precipitationProbability: Int,
    val pressure: Int,
    val humidity: Int,
    val windDirection: Int,
    val windPower: Int
)
