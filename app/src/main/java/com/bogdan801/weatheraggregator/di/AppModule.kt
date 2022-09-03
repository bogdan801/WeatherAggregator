package com.bogdan801.weatheraggregator.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
/*    *//**
     * Method that provides BaseApplication
     *//*
    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): BaseApplication {
        return app as BaseApplication
    }

    *//**
     * Method that provides a database
     *//*
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context) =
        Room.databaseBuilder(app, Database::class.java, "database")
            .createFromAsset("database/database.db")
            .build()

    *//**
     * Method that provides a data access object
     *//*
    @Provides
    fun provideDao(db :Database) = db.dbDao

    *//**
     * Method that provides a Repository
     *//*
    @Provides
    @Singleton
    fun provideRepository(db: Database): Repository {
        return RepositoryImpl(db.dbDao)
    }*/
}