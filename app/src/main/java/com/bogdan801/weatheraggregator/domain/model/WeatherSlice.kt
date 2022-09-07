package com.bogdan801.weatheraggregator.domain.model

sealed class Wind{
    data class North    (val power: Int): Wind()
    data class NorthEast(val power: Int): Wind()
    data class East     (val power: Int): Wind()
    data class SouthEast(val power: Int): Wind()
    data class South    (val power: Int): Wind()
    data class SouthWest(val power: Int): Wind()
    data class West     (val power: Int): Wind()
    data class NorthWest(val power: Int): Wind()
}

data class WeatherSlice(
    val sliceID: Int = 0,
    val dayID: Int = 0,
    val time: String = "00:00",
    val skyCondition: SkyCondition = SkyCondition("c_c_0_d"),
    val precipitationProbability: Int = 0,
    val pressure: Int = 760,
    val humidity: Int = 0,
    val wind: Wind = Wind.North(0)
)
