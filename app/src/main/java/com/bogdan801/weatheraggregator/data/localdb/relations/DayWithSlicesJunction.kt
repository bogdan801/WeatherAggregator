package com.bogdan801.weatheraggregator.data.localdb.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.bogdan801.weatheraggregator.data.localdb.entities.DayWeatherEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherSliceEntity

data class DayWithSlicesJunction(
    @Embedded
    val dayWeatherEntity: DayWeatherEntity,
    @Relation(
        parentColumn = "dayID",
        entityColumn = "dayID"
    )
    val slices: List<WeatherSliceEntity>
)
