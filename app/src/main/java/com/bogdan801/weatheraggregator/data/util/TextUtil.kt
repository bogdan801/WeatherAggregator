package com.bogdan801.weatheraggregator.data.util

fun Int.toDegree(): String = if (this>0) "+$this°" else "$this°"
