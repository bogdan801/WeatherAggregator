package com.bogdan801.weatheraggregator.data.mapper

import com.bogdan801.weatheraggregator.data.localdb.entities.DayWeatherEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherDataEntity
import com.bogdan801.weatheraggregator.data.localdb.entities.WeatherSliceEntity
import com.bogdan801.weatheraggregator.data.localdb.relations.DataWithDaysJunction
import com.bogdan801.weatheraggregator.data.localdb.relations.DayWithSlicesJunction
import com.bogdan801.weatheraggregator.domain.model.*
import kotlinx.datetime.toLocalDate

fun WeatherSliceEntity.toWeatherSlice(): WeatherSlice = WeatherSlice(
    sliceID = sliceID,
    dayID = dayID,
    time = time,
    skyCondition = SkyCondition(skyCondition),
    precipitationProbability = precipitationProbability,
    pressure = pressure,
    humidity = humidity,
    wind = when(windDirection){
        0 -> Wind.North(windPower)
        1 -> Wind.NorthEast(windPower)
        2 -> Wind.East(windPower)
        3 -> Wind.SouthEast(windPower)
        4 -> Wind.South(windPower)
        5 -> Wind.SouthWest(windPower)
        6 -> Wind.West(windPower)
        7 -> Wind.NorthWest(windPower)
        else -> Wind.North(0)
    }
)

fun WeatherSlice.toWeatherSliceEntity(): WeatherSliceEntity = WeatherSliceEntity(
    sliceID = sliceID,
    dayID = dayID,
    time = time,
    skyCondition = skyCondition.descriptor,
    precipitationProbability = precipitationProbability,
    pressure = pressure,
    humidity = humidity,
    windDirection = when(wind){
        is Wind.North     -> 0
        is Wind.NorthEast -> 1
        is Wind.East      -> 2
        is Wind.SouthEast -> 3
        is Wind.South     -> 4
        is Wind.SouthWest -> 5
        is Wind.West      -> 6
        is Wind.NorthWest -> 7
    },
    windPower = when(wind){
        is Wind.North     -> wind.power
        is Wind.NorthEast -> wind.power
        is Wind.East      -> wind.power
        is Wind.SouthEast -> wind.power
        is Wind.South     -> wind.power
        is Wind.SouthWest -> wind.power
        is Wind.West      -> wind.power
        is Wind.NorthWest -> wind.power
    }
)

fun DayWeatherEntity.toDayWeatherCondition(): DayWeatherCondition = DayWeatherCondition(
    dayID = dayID,
    dataID = dataID,
    date = date.toLocalDate(),
    skyCondition = SkyCondition(skyCondition),
    dayTemperature = dayTemperature,
    nightTemperature = nightTemperature,
)

fun DayWeatherCondition.toDayWeatherEntity(): DayWeatherEntity = DayWeatherEntity(
    dayID = dayID,
    dataID = dataID,
    date = date.toString(),
    skyCondition = skyCondition.descriptor,
    dayTemperature = dayTemperature,
    nightTemperature = nightTemperature
)

fun WeatherDataEntity.toWeatherData(): WeatherData = WeatherData(
    dataID = dataID,
    currentDate = currentDate.toLocalDate(),
    currentLocation = currentLocation,
    domain = WeatherSourceDomain.values()[domain],
    url = url,
    currentSkyCondition = SkyCondition(currentSkyCondition),
    currentTemperature = currentTemperature,
)

fun WeatherData.toWeatherDataEntity(): WeatherDataEntity = WeatherDataEntity(
    dataID = dataID,
    currentDate = currentDate.toString(),
    currentLocation = currentLocation,
    domain = domain.ordinal,
    url = url,
    currentSkyCondition = currentSkyCondition.descriptor,
    currentTemperature = currentTemperature
)

fun DayWithSlicesJunction.toDayWeatherCondition(): DayWeatherCondition = DayWeatherCondition(
    dayID = dayWeatherEntity.dayID,
    dataID = dayWeatherEntity.dataID,
    date = dayWeatherEntity.date.toLocalDate(),
    skyCondition = SkyCondition(dayWeatherEntity.skyCondition),
    dayTemperature = dayWeatherEntity.dayTemperature,
    nightTemperature = dayWeatherEntity.nightTemperature,
    weatherByHours = slices.map { it.toWeatherSlice() }
)


fun DataWithDaysJunction.toWeatherData(): WeatherData = WeatherData(
    dataID = weatherDataEntity.dataID,
    currentDate = weatherDataEntity.currentDate.toLocalDate(),
    currentLocation = weatherDataEntity.currentLocation,
    domain = WeatherSourceDomain.values()[weatherDataEntity.domain],
    url = weatherDataEntity.url,
    currentSkyCondition = SkyCondition(weatherDataEntity.currentSkyCondition),
    currentTemperature = weatherDataEntity.currentTemperature,
    weatherByDates = days.map { it.toDayWeatherCondition() }
)