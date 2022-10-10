package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetWeatherDataUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(location: Location, domain: WeatherSourceDomain): Flow<Resource<WeatherData>> = flow {
        emit(Resource.Loading())

        val cachedData = repository.getWeatherDataByDomain(domain).first()

        emit(Resource.Loading(data = cachedData))

        try {
            val networkData = repository.getWeatherDataFromNetwork(domain, location)

            repository.deleteWeatherDataByDomain(domain)
            repository.insertWeatherData(networkData)

            repository.getWeatherDataByDomain(domain).collect{ data ->
                emit(Resource.Success(data = data))
            }
            //emit(Resource.Success(data = networkData))
        }
        catch (e: WrongUrlException){
            emit(
                Resource.Error(
                    message = "Даний населений пункт не знайдено",
                    data = cachedData
                )
            )
        }
        catch (e: NoConnectionException){
            emit(
                Resource.Error(
                    message = "Відсутнє з'єднання з інтернетом",
                    data = cachedData
                )
            )
        }
    }
}

