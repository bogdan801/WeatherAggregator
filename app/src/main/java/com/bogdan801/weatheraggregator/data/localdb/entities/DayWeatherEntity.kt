package com.bogdan801.weatheraggregator.data.localdb.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = WeatherDataEntity::class,
            parentColumns = ["dataID"],
            childColumns = ["dataID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DayWeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val dayID: Int,
    val dataID: Int,
    val date: String,
    val skyCondition: String,
    val dayTemperature: Int = 0,
    val nightTemperature: Int = 0
)
