package com.bogdan801.weatheraggregator.di

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.room.Room
import com.bogdan801.weatheraggregator.data.datastore.readIntFromDataStore
import com.bogdan801.weatheraggregator.data.datastore.saveIntToDataStore
import com.bogdan801.weatheraggregator.data.localdb.Database
import com.bogdan801.weatheraggregator.data.remote.api.OpenWeatherApi
import com.bogdan801.weatheraggregator.data.repository.RepositoryImpl
import com.bogdan801.weatheraggregator.domain.model.WeatherSourceDomain
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.domain.usecase.GetAverageWeatherDataUseCase
import com.bogdan801.weatheraggregator.domain.usecase.GetWeatherDataUseCase
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): BaseApplication {
        return app as BaseApplication
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(app, Database::class.java, "database")
            .createFromAsset("database/database.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideOpenWeatherApi(): OpenWeatherApi {
        return Retrofit.Builder()
            .baseUrl(WeatherSourceDomain.OpenWeather.domain)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherApi::class.java)
    }

    @Provides
    fun provideDao(db: Database) = db.dbDao

    @Provides
    @Singleton
    fun provideRepository(db: Database, api: OpenWeatherApi): Repository {
        return RepositoryImpl(db.dbDao, api)
    }

    @Provides
    @Singleton
    fun provideThemeState(@ApplicationContext context: Context): MutableState<Theme> {
        var savedTheme: Int?
        runBlocking {
            savedTheme = context.readIntFromDataStore("THEME")
        }

        val theme = if(savedTheme == null){
            runBlocking {
                context.saveIntToDataStore("THEME", 0)
            }
            Theme.Auto
        } else{
            Theme.values()[savedTheme!!]
        }

        return mutableStateOf(theme)
    }

    @Provides
    @Singleton
    fun provideGetWeatherDataUseCase(repository: Repository): GetWeatherDataUseCase = GetWeatherDataUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAverageWeatherDataUseCase(): GetAverageWeatherDataUseCase = GetAverageWeatherDataUseCase()
}