package com.bogdan801.weatheraggregator.data.remote

class NoConnectionException(error: String): Exception(error)
class WrongUrlException(error: String): Exception(error)
