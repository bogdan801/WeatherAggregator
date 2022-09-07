package com.bogdan801.weatheraggregator.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.text.isDigitsOnly
import com.bogdan801.weatheraggregator.domain.model.Cloudiness.*

data class SkyCondition(
    private var _cloudiness: Cloudiness = Clear,
    private var _precipitation: Precipitation = Precipitation.None,
    private var _timeOfDay: TimeOfDay = TimeOfDay.Day
){
    private var imageName = "ic_c_c_0_d"

    val cloudiness get() = _cloudiness
    val precipitation get() = _precipitation
    val timeOfDay get() = _timeOfDay
    val descriptor get() = imageName.substring(3)

    constructor(descriptor: String):this() {
        val parts = descriptor.split('_')
        if(parts.size != 4) return

        val cloudiness = when(parts[0]){
            "c" -> Clear
            "1" -> LittleCloudy
            "2" -> CloudyWithClearing
            "3" -> Cloudy
            "4" -> Gloomy
            else -> return
        }

        if(!parts[2].isDigitsOnly()) return

        val precipitation = when(parts[1]){
            "c" -> Precipitation.None
            "r" -> {
                val level = parts[2].toInt()
                if(level !in 0..6) return
                Precipitation.Rain(RainLevel.values()[level-1])
            }
            "rs" -> {
                val level = parts[2].toInt()
                if(level !in 0..4) return
                Precipitation.RainWithSnow(RainWithSnowLevel.values()[level-1])
            }
            "s" -> {
                val level = parts[2].toInt()
                if(level !in 0..6) return
                Precipitation.Snow(SnowLevel.values()[level-1])
            }
            else -> return
        }

        val timeOfDay = when(parts[3]){
            "d"-> TimeOfDay.Day
            "n"-> TimeOfDay.Night
            "dn"-> TimeOfDay.Day
            else -> return
        }

        _cloudiness = cloudiness
        _precipitation = precipitation
        _timeOfDay = timeOfDay

        imageName = getResourceName()
    }

    init {
        imageName = getResourceName()
    }

    private fun getResourceName(): String = buildString {
        append("ic_")
        when (_cloudiness) {
            Clear -> {
                when(_timeOfDay){
                    TimeOfDay.Day -> append("c_c_0_d")
                    TimeOfDay.Night -> append("c_c_0_n")
                }
            }
            LittleCloudy -> append("1_")
            CloudyWithClearing -> append("2_")
            Cloudy -> append("3_")
            Gloomy -> append("4_")
        }
        if(_cloudiness == Clear) return@buildString

        when (_precipitation) {
            is Precipitation.None -> append("c_0_")
            is Precipitation.Rain -> {
                when ((_precipitation as Precipitation.Rain).level) {
                    RainLevel.One -> append("r_1_")
                    RainLevel.Two -> append("r_2_")
                    RainLevel.Three -> append("r_3_")
                    RainLevel.Four -> append("r_4_")
                    RainLevel.Five -> append("r_5_")
                    RainLevel.Thunder -> append("r_6_")
                }
            }
            is Precipitation.RainWithSnow -> {
                when ((_precipitation as Precipitation.RainWithSnow).level) {
                    RainWithSnowLevel.One -> append("rs_1_")
                    RainWithSnowLevel.Two -> append("rs_2_")
                    RainWithSnowLevel.Three -> append("rs_3_")
                    RainWithSnowLevel.Thunder -> append("rs_4_")
                }
            }
            is Precipitation.Snow -> {
                when ((_precipitation as Precipitation.Snow).level) {
                    SnowLevel.One -> append("s_1_")
                    SnowLevel.Two -> append("s_2_")
                    SnowLevel.Three -> append("s_3_")
                    SnowLevel.Four -> append("s_4_")
                    SnowLevel.Five -> append("s_5_")
                    SnowLevel.Thunder -> append("s_6_")
                }
            }
        }
        if(_cloudiness == Cloudy || _cloudiness == Gloomy){
            append("dn")
        }
        else{
            when(_timeOfDay){
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