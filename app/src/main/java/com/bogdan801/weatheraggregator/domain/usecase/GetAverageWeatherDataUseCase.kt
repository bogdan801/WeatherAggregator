package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.domain.model.*
import kotlin.math.roundToInt


class GetAverageWeatherDataUseCase {
    operator fun invoke(dataList: List<WeatherData>, trustLevels: List<Double>){
        val sum = trustLevels.sum()
        val normalizedTrustLevels = trustLevels.map { it/sum }



        WeatherData(
            currentDate = dataList[0].currentDate,
            currentLocation = dataList[0].currentLocation,
            domain = WeatherSourceDomain.Average,
            url = "average",
            currentSkyCondition = averageOut(dataList.map { it.currentSkyCondition }, trustLevels),
            currentTemperature = averageOut(dataList.map { it.currentTemperature }, trustLevels)
        )

    }

    private fun averageOut(dataList: List<Int>, trustLevels: List<Double>): Int {
        var sum = 0.0
        dataList.forEachIndexed { index, n ->
            sum += n * trustLevels[index]
        }
        return sum.roundToInt()
    }

    private fun averageOut(dataList: List<Double>, trustLevels: List<Double>): Double {
        var sum = 0.0
        dataList.forEachIndexed { index, n ->
            sum += n * trustLevels[index]
        }
        return sum
    }

    private fun averageOut(dataList: List<SkyCondition>, trustLevels: List<Double>): SkyCondition{
        val averageCloudiness = Cloudiness.values()[averageOut(dataList.map { it.cloudiness.ordinal }, trustLevels)]
        val averagePrecipitationType = averageOut(dataList.map { it.precipitation.getOrdinal() }, trustLevels)
        val averagePrecipitationPower = averageOut(dataList.map { it.precipitation.getPower() }, trustLevels)
        val averagePrecipitation = Precipitation.getPrecipitation(averagePrecipitationType, averagePrecipitationPower)
        val averageTimeOfDay = TimeOfDay.values()[averageOut(dataList.map { it.timeOfDay.ordinal }, trustLevels)]
        return SkyCondition(averageCloudiness, averagePrecipitation, averageTimeOfDay)
    }
}