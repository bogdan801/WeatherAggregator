package com.bogdan801.weatheraggregator.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.bogdan801.weatheraggregator.domain.model.Cloudiness.*

data class SkyCondition(
    val cloudiness: Cloudiness = Clear,
    val precipitation: Precipitation = Precipitation.None,
    val timeOfDay: TimeOfDay = TimeOfDay.Day
){
    private var imageName = "ic_c_c_0_d"

    init {
        imageName = getResourceName()
    }

    private fun getResourceName(): String = buildString {
        append("ic_")
        when (cloudiness) {
            Clear -> {
                when(timeOfDay){
                    TimeOfDay.Day -> append("c_c_0_d")
                    TimeOfDay.Night -> append("c_c_0_n")
                }
            }
            LittleCloudy -> append("1_")
            CloudyWithClearing -> append("2_")
            Cloudy -> append("3_")
            Gloomy -> append("4_")
        }
        if(cloudiness == Clear) return@buildString

        when (precipitation) {
            is Precipitation.None -> append("c_0_")
            is Precipitation.Rain -> {
                when (precipitation.level) {
                    RainLevel.One -> append("r_1_")
                    RainLevel.Two -> append("r_2_")
                    RainLevel.Three -> append("r_3_")
                    RainLevel.Four -> append("r_4_")
                    RainLevel.Five -> append("r_5_")
                    RainLevel.Thunder -> append("r_6_")
                }
            }
            is Precipitation.RainWithSnow -> {
                when (precipitation.level) {
                    RainWithSnowLevel.One -> append("rs_1_")
                    RainWithSnowLevel.Two -> append("rs_2_")
                    RainWithSnowLevel.Three -> append("rs_3_")
                    RainWithSnowLevel.Thunder -> append("rs_4_")
                }
            }
            is Precipitation.Snow -> {
                when (precipitation.level) {
                    SnowLevel.One -> append("s_1_")
                    SnowLevel.Two -> append("s_2_")
                    SnowLevel.Three -> append("s_3_")
                    SnowLevel.Four -> append("s_4_")
                    SnowLevel.Five -> append("s_5_")
                    SnowLevel.Thunder -> append("s_6_")
                }
            }
        }
        if(cloudiness == Cloudy || cloudiness == Gloomy){
            append("dn")
        }
        else{
            when(timeOfDay){
                TimeOfDay.Day -> append("d")
                TimeOfDay.Night -> append("n")
            }
        }
    }

    @Composable
    fun getPainterResource(): Painter {
        val context = LocalContext.current
        return painterResource(
            id = context.resources.getIdentifier(imageName, "drawable", context.packageName)
        )
    }
}

enum class Cloudiness{
    Clear,
    LittleCloudy,
    CloudyWithClearing,
    Cloudy,
    Gloomy
}

enum class TimeOfDay{
    Day,
    Night
}

enum class RainLevel{
    One,
    Two,
    Three,
    Four,
    Five,
    Thunder
}

enum class SnowLevel{
    One,
    Two,
    Three,
    Four,
    Five,
    Thunder
}

enum class RainWithSnowLevel{
    One,
    Two,
    Three,
    Thunder
}

sealed class Precipitation{
    object None: Precipitation()
    data class Rain(val level: RainLevel = RainLevel.One): Precipitation()
    data class Snow(val level: SnowLevel = SnowLevel.One): Precipitation()
    data class RainWithSnow(val level: RainWithSnowLevel = RainWithSnowLevel.One): Precipitation()
}


