package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.domain.util.Resource
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import javax.inject.Inject

class GetWeatherDataUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(location: Location, domain: WeatherSourceDomain): Flow<WeatherDataState> = flow {
        emit(WeatherDataState.IsLoading())

        val dataFromDB = repository.getWeatherDataByDomain(domain).first()
        val cachedData = if(dataFromDB.isEmpty) null else dataFromDB

        emit(WeatherDataState.IsLoading(d = cachedData ?: WeatherData()))

        try {
            val networkData = repository.getWeatherDataFromNetwork(domain, location)

            repository.insertWeatherData(networkData)

            repository.getWeatherDataByDomain(domain).cancellable().collect{ data ->
                emit(WeatherDataState.Data(d = data))
            }
        }
        catch (e: WrongUrlException){
            emit(
                WeatherDataState.Error(
                    message = "Даний населений пункт не знайдено",
                    d = cachedData ?: WeatherData()
                )
            )
        }
        catch (e: NoConnectionException){
            emit(
                WeatherDataState.Error(
                    message = "Відсутнє з'єднання з інтернетом",
                    d = cachedData ?: WeatherData()
                )
            )
        }
    }
}

