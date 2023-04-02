package com.bogdan801.weatheraggregator.domain.model

class LocationManager(
    private val allLocations: List<Oblast>
) {
    fun getOblastsList(): List<Oblast> = allLocations

    fun getRegionsOfOblast(oblastIndex: Int) = allLocations[oblastIndex].listOfRegions

    fun getLocationsOfRegion(oblastIndex: Int, regionIndex: Int) = allLocations[oblastIndex].listOfRegions[regionIndex].locations

    fun getLocation(oblastIndex: Int, regionIndex: Int, locationIndex: Int) = allLocations[oblastIndex].listOfRegions[regionIndex].locations[locationIndex]

    fun findMatches(name: Location): List<Location>{
        TODO()
    }
}