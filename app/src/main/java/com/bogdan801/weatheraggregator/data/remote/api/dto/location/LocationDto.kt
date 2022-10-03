package com.bogdan801.weatheraggregator.data.remote.api.dto.location

data class LocationDto(
    val country: String,
    val lat: Double,
    val local_names: LocalNames,
    val lon: Double,
    val name: String
)