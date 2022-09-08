package com.bogdan801.weatheraggregator.data.localdb.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = DayWeatherEntity::class,
            parentColumns = ["dayID"],
            childColumns = ["dayID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
