package com.bogdan801.weatheraggregator.domain.model

import android.content.Context
import com.bogdan801.weatheraggregator.domain.util.readTextFromAsset
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class Oblast(
    val link: String,
    val name: String,
    var listOfRegions: List<Region> = listOf()
)

data class Region(
    val link: String,
    val name: String,
    var locations: List<Location> = listOf()
)

data class Location(
    val metaLink: String,
    val sinoptikLink: String,
    val name: String,
    val regionName: String,
    val oblastName: String,
    val lat: Double,
    val lon: Double
)

fun turnJSONtoOblastList(jsonString: String): List<Oblast>{
    val gson = Gson()
    val type: Type = object : TypeToken<List<Oblast>>() {}.type
    return gson.fromJson(jsonString, type)
}

fun getOblastListFromFile(context: Context): List<Oblast>{
    val jsonString = context.readTextFromAsset("locations.json")
    return turnJSONtoOblastList(jsonString)
}

data class LocationInfo(
    val lat: Double,
    val lon: Double,
    val name: String,
    val state: String?
)
