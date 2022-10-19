package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.data.util.timeToHoursInt
import com.bogdan801.weatheraggregator.domain.model.*
import kotlin.math.roundToInt

class GetAverageWeatherDataUseCase {
    operator fun invoke(dataList: List<WeatherData>, trustLevels: List<Double>): WeatherData{
        if(dataList.isEmpty()) return WeatherData()
        if(dataList.size != trustLevels.size) return WeatherData()

        val sum = trustLevels.sum()
        val normalizedTrustLevels = trustLevels.map { it/sum }


        val allDayWithDomainIdPairs = mutableListOf<Pair<DayWeatherCondition, Int>>()

        dataList.forEachIndexed { id, data ->
            data.weatherByDates.forEach { day ->
                allDayWithDomainIdPairs.add(day to id)
            }
        }

        val groupedList = allDayWithDomainIdPairs.groupBy {
            it.first.date
        }

        val averageDaysList = mutableListOf<DayWeatherCondition>()

        groupedList.keys.forEach { date ->
            val days = groupedList[date]!!.map { it.first }
            val levels = groupedList[date]!!.map { normalizedTrustLevels[it.second] }
            averageDaysList.add(
                averageOut(days, levels)
            )
        }

        return WeatherData(
            currentDate = dataList[0].currentDate,
            currentLocation = dataList[0].currentLocation,
            domain = WeatherSourceDomain.Average,
            url = "average",
            currentSkyCondition = averageOut(dataList.map { it.currentSkyCondition }, normalizedTrustLevels),
            currentTemperature = averageOut(dataList.map { it.currentTemperature }, normalizedTrustLevels),
            weatherByDates = averageDaysList
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

    private fun averageOut(dataList: List<WeatherSlice>, levels: List<Double>): WeatherSlice{
        val sum = levels.sum()
        val trustLevels = levels.map { it/sum }

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

    private fun averageOut(dataList: List<DayWeatherCondition>, levels: List<Double>): DayWeatherCondition{
        val sum = levels.sum()
        val trustLevels = levels.map { it/sum }

        val averageSlicesList = let{
            val listOfSlices = mutableListOf<MutableList<Pair<WeatherSlice, Int>>>()

            for(i in 0..23 step 3){
                listOfSlices.add(mutableListOf())
                dataList.forEachIndexed { index, item ->
                    val foundSlices = mutableListOf<WeatherSlice>()
                    for (j in 0..2){
                        val foundSlice = item.weatherByHours.find { it.time.timeToHoursInt() == i+j}
                        if(foundSlice != null) {
                            foundSlices.add(foundSlice)
                        }
                    }
                    if(foundSlices.isNotEmpty()){
                        val averagedSliceByHours = averageOut(foundSlices, List(foundSlices.size){ 1.0/foundSlices.size })
                        listOfSlices[listOfSlices.lastIndex].add(
                            averagedSliceByHours to index
                        )
                    }
                }
            }

            val outputList = mutableListOf<WeatherSlice>()

            listOfSlices.forEach{ listOfPairs ->
                if(listOfPairs.isNotEmpty()){
                    val slicesList = listOfPairs.map { it.first }
                    val trustValuesList  = listOfPairs.map { it.second }.map { id -> trustLevels[id] }
                    outputList.add(
                        averageOut(slicesList, trustValuesList)
                    )
                }
            }

            return@let outputList.toList()
        }

        return DayWeatherCondition(
            date = dataList[0].date,
            skyCondition = averageOut(dataList.map { it.skyCondition }, trustLevels),
            dayTemperature = averageOut(dataList.map { it.dayTemperature }, trustLevels),
            nightTemperature = averageOut(dataList.map { it.nightTemperature }, trustLevels),
            weatherByHours = averageSlicesList
        )
    }
}