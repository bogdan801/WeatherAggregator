package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain


class GetAverageWeatherDataUseCase {
    operator fun invoke(dataList: List<WeatherData>, trustLevels: List<Double>){
        val sum = trustLevels.sum()
        val normalizedTrustLevels = trustLevels.map { it/sum }

        val cloudinessList = dataList.map { it.currentSkyCondition.cloudiness.ordinal }

        WeatherData(
            currentDate = dataList[0].currentDate,
            currentLocation = dataList[0].currentLocation,
            domain = WeatherSourceDomain.Average,
            url = "average"

        )

    }

    private fun averageOut(dataList: List<Double>, trustLevels: List<Double>): Double {
        var sum: Double = 0.0
        dataList.forEachIndexed { index, t ->
            sum += t * trustLevels[index]
        }
        return sum
    }
}