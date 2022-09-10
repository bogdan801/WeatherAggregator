package com.bogdan801.weatheraggregator.domain.util

import android.content.Context

fun Context.readTextFromAsset(fileName: String): String{
    return assets.open(fileName).bufferedReader().use {
        it.readText()
    }
}