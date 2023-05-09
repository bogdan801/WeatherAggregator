package com.bogdan801.weatheraggregator.domain.model

data class Location(
    val metaLink: String,
    val sinoptikLink: String,
    val name: String,
    val regionName: String,
    val oblastName: String,
    val lat: Double,
    val lon: Double
){
    fun isNotEmpty() = name.isNotBlank()

    override fun toString() = "${metaLink}*${sinoptikLink}*${name}*${regionName}*${oblastName}*${lat}*${lon}"

    companion object{
        fun fromString(string: String?): Location?{
            if(string==null) return null

            val parts = string.split("*")
            if(parts.size != 7) return null

            return Location(
                metaLink = parts[0],
                sinoptikLink = parts[1],
                name = parts[2],
                regionName = parts[3],
                oblastName = parts[4],
                lat = parts[5].toDouble(),
                lon = parts[6].toDouble()
            )
        }
    }
}