package com.bogdan801.weatheraggregator.domain.util

sealed class Resource<T>(val data: T? = null, val message: String? = null, val e: Exception? = null) {
    class Loading<T>(data: T? = null): Resource<T>(data)
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(message: String, data: T? = null, e: Exception? = null): Resource<T>(data, message, e)
}