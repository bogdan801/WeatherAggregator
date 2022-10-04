package com.bogdan801.weatheraggregator.data.remote.api.dto.location

import com.bogdan801.weatheraggregator.domain.model.LocationInfo

data class LocationDto(
    val country: String,
    val lat: Double,
    val local_names: LocalNames,
    val lon: Double,
    val name: String,
    val state: String?
){
    fun toLocationInfo(): LocationInfo = LocationInfo(
        lat = lat,
        lon = lon,
        name = name,
        state = state
    )
}

