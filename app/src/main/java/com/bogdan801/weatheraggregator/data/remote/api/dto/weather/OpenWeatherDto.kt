package com.bogdan801.weatheraggregator.data.remote.api.dto.weather

import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime

data class OpenWeatherDto(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<TimeStamp>,
    val message: Int
){
    fun toWeatherData(location: Location): WeatherData {
        val slicesByDays = mutableListOf<MutableList<WeatherSlice>>()
        var dayIndex = 0
        slicesByDays.add(mutableListOf())
        list.forEachIndexed{ index, timeStamp ->
            if(index == 0){
                slicesByDays[dayIndex].add(timeStamp.toWeatherSlice())
            }
            else{
                val currentDate = timeStamp.dt_txt.split(' ')[0]
                val lastDate = list[index-1].dt_txt.split(' ')[0]
                if(currentDate == lastDate){
                    slicesByDays[dayIndex].add(timeStamp.toWeatherSlice())
                }
                else{
                    dayIndex++
                    if(slicesByDays.lastIndex != dayIndex){
                        slicesByDays.add(mutableListOf())
                    }
                    slicesByDays[dayIndex].add(timeStamp.toWeatherSlice())
                }
            }
        }

        val days = mutableListOf<DayWeatherCondition>()
        val dates = list.map { it.dt_txt.split(' ')[0] }.distinct().map { it.toLocalDate() }
        for (i in 0..4){
            days.add(
                DayWeatherCondition(
                    date = dates[i],
                    skyCondition = slicesByDays[i][slicesByDays[i].lastIndex/2].skyCondition,
                    dayTemperature = slicesByDays[i][slicesByDays[i].lastIndex/2].temperature,
                    nightTemperature = slicesByDays[i][slicesByDays[i].lastIndex].temperature,
                    weatherByHours = slicesByDays[i]
                )
            )
        }

        return WeatherData(
            currentDate = days[0].date,
            currentLocation = location.name,
            domain = WeatherSourceDomain.OpenWeather,
            url = "https://api.openweathermap.org/data/2.5/forecast",
            currentSkyCondition = days[0].skyCondition,
            currentTemperature = days[0].dayTemperature,
            weatherByDates = days.toList()
        )
    }

}