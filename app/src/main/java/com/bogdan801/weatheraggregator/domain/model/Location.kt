package com.bogdan801.weatheraggregator.domain.model

data class Location(
    val metaLink: String,
    val sinoptikLink: String,
    val name: String,
    val regionName: String,
    val oblastName: String,
    val lat: Double,
    val lon: Double
)