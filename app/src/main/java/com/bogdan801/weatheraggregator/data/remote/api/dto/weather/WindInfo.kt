package com.bogdan801.weatheraggregator.data.remote.api.dto.weather

import com.bogdan801.weatheraggregator.domain.model.Wind

data class WindInfo(
    val deg: Int,
    val gust: Double,
    val speed: Double
){
    fun toWind(): Wind{
        val dir: String = when{
            (deg in 0..22 || deg in 338..360) -> "N"
            deg in 23..67 -> "NE"
            deg in 68..112 -> "E"
            deg in 113..157 -> "SE"
            deg in 158..202 -> "S"
            deg in 203..247 -> "SW"
            deg in 248..292 -> "W"
            deg in 293..337-> "NW"
            else -> "N"
        }
        val pow: Int = speed.toInt()

        return Wind.get(dir, pow)
    }
}