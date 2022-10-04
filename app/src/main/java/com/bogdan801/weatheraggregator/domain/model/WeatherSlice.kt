package com.bogdan801.weatheraggregator.domain.model

data class WeatherSlice(
    val sliceID: Int = 0,
    val dayID: Int = 0,
    val time: String = "00:00",
    val temperature: Int = 0,
    val skyCondition: SkyCondition = SkyCondition("c_c_0_d"),
    val precipitationProbability: Int = 0,
    val pressure: Int = 760,
    val humidity: Int = 0,
    val wind: Wind = Wind.North(0)
)

sealed class Wind{
    data class North    (val power: Int): Wind()
    data class NorthEast(val power: Int): Wind()
    data class East     (val power: Int): Wind()
    data class SouthEast(val power: Int): Wind()
    data class South    (val power: Int): Wind()
    data class SouthWest(val power: Int): Wind()
    data class West     (val power: Int): Wind()
    data class NorthWest(val power: Int): Wind()

    companion object{
        fun get(direction: String, power: Int): Wind  = when(direction){
            "N"  -> North(power)
            "NE" -> NorthEast(power)
            "E"  -> East(power)
            "SE" -> SouthEast(power)
            "S"  -> South(power)
            "SW" -> SouthWest(power)
            "W"  -> West(power)
            "NW" -> NorthWest(power)
            else -> North(power)
        }
    }
}
