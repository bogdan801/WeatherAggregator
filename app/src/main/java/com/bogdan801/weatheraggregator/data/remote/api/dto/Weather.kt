package com.bogdan801.weatheraggregator.data.remote.api.dto

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)