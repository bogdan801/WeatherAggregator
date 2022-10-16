package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.domain.model.*
import kotlin.math.roundToInt

class GetAverageWeatherDataUseCase {
    operator fun invoke(dataList: List<WeatherData>, trustLevels: List<Double>): WeatherData{
        if(dataList.isEmpty()) return WeatherData()
        if(dataList.size != trustLevels.size) return WeatherData()

        val sum = trustLevels.sum()
        val normalizedTrustLevels = trustLevels.map { it/sum }


        return WeatherData(
            currentDate = dataList[0].currentDate,
            currentLocation = dataList[0].currentLocation,
            domain = WeatherSourceDomain.Average,
            url = "average",
            currentSkyCondition = averageOut(dataList.map { it.currentSkyCondition }, normalizedTrustLevels),
            currentTemperature = averageOut(dataList.map { it.currentTemperature }, normalizedTrustLevels)
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

    private fun averageOut(dataList: List<SkyCondition>, trustLevels: List<Double>): SkyCondition {
        val averageCloudiness = Cloudiness.values()[averageOut(dataList.map { it.cloudiness.ordinal }, trustLevels)]
        val averagePrecipitationType = averageOut(dataList.map { it.precipitation.getOrdinal() }, trustLevels)
        val averagePrecipitationPower = averageOut(dataList.map { it.precipitation.getPower() }, trustLevels)
        val averagePrecipitation = Precipitation.getPrecipitation(averagePrecipitationType, averagePrecipitationPower)
        val averageTimeOfDay = TimeOfDay.values()[averageOut(dataList.map { it.timeOfDay.ordinal }, trustLevels)]
        return SkyCondition(averageCloudiness, averagePrecipitation, averageTimeOfDay)
    }

    private fun averageOut(dataList: List<Wind>, trustLevels: List<Double>): Wind{
        val averagePower = averageOut(dataList.map { it.getWindPower() }, trustLevels)
        val averageDir = let{
            val dirs = dataList.map { it.getWindOrdinal() }
            if(dirs.maxOrNull()!! - dirs.minOrNull()!! >= 4){
                val maxIdx = dirs.indices.maxByOrNull { dirs[it] } ?: -1
                val newDirs = dirs.mapIndexed { index, dir ->
                    if(index == maxIdx) dir.toDouble()
                    else dir.toDouble()+8
                }
                val output = averageOut(newDirs, trustLevels)
                return@let if(output >= 8) (output-8).roundToInt() else output.roundToInt()
            }
            else {
                return@let averageOut(dirs.map { it.toDouble() }, trustLevels).roundToInt()
            }
        }
        return Wind.create(averageDir, averagePower)
    }

    private fun averageOut(dataList: List<WeatherSlice>, trustLevels: List<Double>): WeatherSlice{
        val averageTime = dataList.map{ it.time }.groupBy { it }.maxByOrNull { it.value.size }!!.key
        val averagePrecipitation = let {
            val allProbs = dataList.map { it.precipitationProbability }
            when (allProbs.count{ it == -1 }) {
                allProbs.size -> return@let -1
                0 -> return@let averageOut(allProbs, trustLevels)
                else -> {
                    val newTrustLevels = mutableListOf<Double>()
                    val filteredProbs = allProbs.filterIndexed { index, i ->
                        if(i != -1){
                            newTrustLevels.add(trustLevels[index])
                        }
                        i != -1
                    }
                    val multiplier = 1/newTrustLevels.sum()
                    val normalizedNewTrustLevels = newTrustLevels.map { multiplier * it }
                    return@let averageOut(filteredProbs, normalizedNewTrustLevels)
                }
            }
        }
        return WeatherSlice(
            time = averageTime,
            temperature = averageOut(dataList.map { it.temperature }, trustLevels),
            skyCondition = averageOut(dataList.map { it.skyCondition }, trustLevels),
            precipitationProbability = averagePrecipitation,
            pressure = averageOut(dataList.map { it.pressure }, trustLevels),
            humidity = averageOut(dataList.map { it.humidity }, trustLevels),
            wind = averageOut(dataList.map { it.wind }, trustLevels)
        )
    }
}