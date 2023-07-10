package com.bogdan801.weatheraggregator.data.util

import android.content.Context
import android.location.LocationManager

fun isLocationON(context: Context): Boolean?{
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)
}