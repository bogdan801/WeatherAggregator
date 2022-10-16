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
        fun create(direction: String, power: Int): Wind  = when(direction){
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

        fun create(direction: Int, power: Int): Wind  = when(direction){
            0  -> North(power)
            1 -> NorthEast(power)
            2  -> East(power)
            3 -> SouthEast(power)
            4  -> South(power)
            5 -> SouthWest(power)
            6  -> West(power)
            7 -> NorthWest(power)
            else -> North(power)
        }
    }

    fun getWindPower(): Int = when(this){
        is North -> power
        is NorthEast -> power
        is East -> power
        is SouthEast -> power
        is South -> power
        is SouthWest -> power
        is West -> power
        is NorthWest -> power
    }

    fun getWindOrdinal(): Int = when(this){
        is North     -> 0
        is NorthEast -> 1
        is East      -> 2
        is SouthEast -> 3
        is South     -> 4
        is SouthWest -> 5
        is West      -> 6
        is NorthWest -> 7
    }
}
