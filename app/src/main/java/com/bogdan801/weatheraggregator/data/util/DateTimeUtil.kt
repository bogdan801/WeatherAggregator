package com.bogdan801.weatheraggregator.data.util

import androidx.core.text.isDigitsOnly
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import java.time.YearMonth

class DateTimeUtilException(error: String): Exception(error)

fun getCurrentDate(): LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date

fun LocalDate.toFormattedString() =
    "${dayOfMonth.toString().padStart(2, '0')}.${monthNumber.toString().padStart(2, '0')}.$year"

fun LocalDateTime.toFormattedTime() =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

fun String.timeToHoursInt(): Int {
    val parts = split(":")
    if(parts.size != 2) throw DateTimeUtilException("Wrong time format: $this, it should be \"hh:mm\"")
    if(!parts[0].isDigitsOnly()) throw DateTimeUtilException("Wrong time format: $this, first part should be digit only")

    return parts[0].toInt()
}


