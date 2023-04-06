package com.bogdan801.weatheraggregator.data.localdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val locationID: Int,
    val metaLink: String,
    val sinoptikLink: String,
    val name: String,
    val regionName: String,
    val oblastName: String,
    val lat: Double,
    val lon: Double
)
