package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.domain.util.Resource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import javax.inject.Inject

class GetWeatherDataUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(location: Location, domain: WeatherSourceDomain): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading())

        val dataFromDB = repository.getWeatherDataByDomain(domain).first()
        val cachedData = if(dataFromDB.isEmpty) null else dataFromDB

        emit(Resource.Loading(data = cachedData))

        try {
            val networkData = repository.getWeatherDataFromNetwork(domain, location)

            repository.insertWeatherData(networkData)

            repository.getWeatherDataByDomain(domain).cancellable().collect{ data ->
                emit(Resource.Success(data = data))
            }
        }
        catch (e: WrongUrlException){
            emit(
                Resource.Error(
                    message = "Даний населений пункт не знайдено",
                    data = cachedData,
                    e = e
                )
            )
        }
        catch (e: NoConnectionException){
            emit(
                Resource.Error(
                    message = "Відсутнє з'єднання з інтернетом",
                    data = cachedData,
                    e = e
                )
            )
        }
    }
}

