package com.bogdan801.weatheraggregator.domain.usecase

import com.bogdan801.weatheraggregator.data.remote.NoConnectionException
import com.bogdan801.weatheraggregator.data.remote.ParsingException
import com.bogdan801.weatheraggregator.data.remote.WrongUrlException
import com.bogdan801.weatheraggregator.data.util.getCurrentDate
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.model.WeatherData
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.screens.home.WeatherDataState
import kotlinx.coroutines.flow.*
import java.net.SocketTimeoutException
import javax.inject.Inject

class GetWeatherDataUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(location: Location, domain: WeatherSourceDomain): Flow<WeatherDataState> = flow {
        val url = domain.domain + when(domain){
            WeatherSourceDomain.Meta -> location.metaLink
            WeatherSourceDomain.Sinoptik -> location.sinoptikLink
            WeatherSourceDomain.OpenWeather -> "/data/2.5/forecast?lat=${location.lat}&lon=${location.lon}"
            WeatherSourceDomain.Average -> ""
        }

        emit(
            WeatherDataState.IsLoading(
                d = WeatherData(
                    domain = domain,
                    url = url
                )
            )
        )

        val dataFromDB = repository.getWeatherDataByDomain(domain).first()
        val currentDate = getCurrentDate()
        val cachedData =
            if(!dataFromDB.isEmpty && dataFromDB.currentDate == currentDate) dataFromDB
            else WeatherData(domain = domain, url = url)

        emit(WeatherDataState.IsLoading(d = cachedData))

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
                    d = cachedData
                )
            )
        }
        catch (e: NoConnectionException){
            emit(
                WeatherDataState.Error(
                    message = "Відсутнє з'єднання з інтернетом",
                    d = cachedData
                )
            )
        }
        catch (e: SocketTimeoutException){
            emit(
                WeatherDataState.Error(
                    message = "Не вдалося отримати дані, спробуйте ще раз",
                    d = cachedData
                )
            )
        }
        catch (e: ParsingException){
            emit(
                WeatherDataState.Error(
                    message = "Проблема з зчитуванням даних зі сторінки сайту",
                    d = cachedData
                )
            )
        }
    }
}

