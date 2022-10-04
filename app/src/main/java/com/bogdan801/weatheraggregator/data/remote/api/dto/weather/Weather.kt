package com.bogdan801.weatheraggregator.data.remote.api.dto.weather

import com.bogdan801.weatheraggregator.domain.model.*

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
){
    fun toSkyCondition(): SkyCondition{
        val desc = id.toString()
        val condition = desc[0]
        val type = desc[1]
        val level = desc[2]

        val cloudiness = when(condition){
            '2' -> Cloudiness.Gloomy
            '3' -> Cloudiness.Gloomy
            '5' -> when(type){
                '0' -> Cloudiness.LittleCloudy
                '1' -> Cloudiness.CloudyWithClearing
                '2' -> Cloudiness.Cloudy
                '3' -> Cloudiness.Gloomy
                else -> Cloudiness.LittleCloudy
            }
            '6' -> when(type){
                '0' -> Cloudiness.CloudyWithClearing
                '1' -> Cloudiness.Cloudy
                '2' -> Cloudiness.Gloomy
                else -> Cloudiness.LittleCloudy
            }
            '7' -> Cloudiness.Cloudy
            '8' -> when(level){
                '0' -> Cloudiness.Clear
                '1' -> Cloudiness.LittleCloudy
                '2' -> Cloudiness.CloudyWithClearing
                '3' -> Cloudiness.Cloudy
                '4' -> Cloudiness.Gloomy
                else -> Cloudiness.Clear
            }
            else -> Cloudiness.Clear
        }

        val precipitation: Precipitation = when(condition){
            '2' -> Precipitation.Rain(RainLevel.Thunder)
            '3' -> when(type){
                '0' -> when(level){
                    '0' -> Precipitation.Rain(RainLevel.One)
                    '1' -> Precipitation.Rain(RainLevel.Two)
                    '2' -> Precipitation.Rain(RainLevel.Three)
                    else -> Precipitation.Rain(RainLevel.One)
                }
                '1' -> when(level){
                    '0' -> Precipitation.Rain(RainLevel.One)
                    '1' -> Precipitation.Rain(RainLevel.Two)
                    '2' -> Precipitation.Rain(RainLevel.Three)
                    '3' -> Precipitation.Rain(RainLevel.Four)
                    '4' -> Precipitation.Rain(RainLevel.Five)
                    else -> Precipitation.Rain(RainLevel.One)
                }
                '2' -> Precipitation.Rain(RainLevel.Four)
                else -> Precipitation.Rain(RainLevel.One)
            }
            '5' ->  when(type){
                '0' -> when(level){
                    '0' -> Precipitation.Rain(RainLevel.One)
                    '1' -> Precipitation.Rain(RainLevel.Two)
                    '2' -> Precipitation.Rain(RainLevel.Three)
                    '3' -> Precipitation.Rain(RainLevel.Four)
                    '4' -> Precipitation.Rain(RainLevel.Five)
                    else -> Precipitation.Rain(RainLevel.One)
                }
                '1' -> Precipitation.Rain(RainLevel.Three)
                '2' -> when(level){
                    '0' -> Precipitation.Rain(RainLevel.Three)
                    '1' -> Precipitation.Rain(RainLevel.Four)
                    '2' -> Precipitation.Rain(RainLevel.Five)
                    else ->Precipitation.Rain(RainLevel.One)
                }
                '3' -> Precipitation.Rain(RainLevel.Five)
                else -> Precipitation.Rain(RainLevel.One)
            }
            '6' -> when(type){
                '0' -> when(level){
                    '0' -> Precipitation.Snow(SnowLevel.One)
                    '1' -> Precipitation.Snow(SnowLevel.Two)
                    '2' -> Precipitation.Snow(SnowLevel.Four)
                    else -> Precipitation.Snow(SnowLevel.One)
                }
                '1' -> when(level){
                    '1' -> Precipitation.Snow(SnowLevel.One)
                    '2' -> Precipitation.Snow(SnowLevel.Two)
                    '3' -> Precipitation.Snow(SnowLevel.Three)
                    '5' -> Precipitation.RainWithSnow(RainWithSnowLevel.One)
                    '6' -> Precipitation.RainWithSnow(RainWithSnowLevel.Three)
                    else -> Precipitation.Snow(SnowLevel.One)
                }
                '2' -> when(level){
                    '0' -> Precipitation.Snow(SnowLevel.Three)
                    '1' -> Precipitation.Snow(SnowLevel.Four)
                    '2' -> Precipitation.Snow(SnowLevel.Five)
                    else -> Precipitation.Snow(SnowLevel.One)
                }
                else -> Precipitation.Rain(RainLevel.One)
            }
            '7' -> Precipitation.None
            '8' -> Precipitation.None
            else -> Precipitation.None
        }

        val timeOfDay = if(icon[2] == 'n') TimeOfDay.Night else TimeOfDay.Day

        return SkyCondition(
            _cloudiness = cloudiness,
            _precipitation = precipitation,
            _timeOfDay = timeOfDay
        )
    }
}