package com.bogdan801.weatheraggregator.data.localdb.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bogdan801.weatheraggregator.data.localdb.entities.DayWeatherEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherDataEntity

data class DataWithDaysJunction(
    @Embedded
    val weatherDataEntity: WeatherDataEntity,
    @Relation(
        parentColumn = "dataID",
        entityColumn = "dataID"
    )
    val days: List<DayWeatherEntity>
)
