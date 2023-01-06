package com.bogdan801.weatheraggregator.data.util

import android.content.Context
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

fun getShortMonthName(monthNumber: Int, context: Context): String = when(monthNumber){
    1 ->  "Jan"
    2 ->  "Feb"
    3 ->  "Mar"
    4 ->  "Apr"
    5 ->  "May"
    6 ->  "Jun"
    7 ->  "Jul"
    8 ->  "Aug"
    9 ->  "Sep"
    10 -> "Oct"
    11 -> "Nov"
    12 -> "Dec"
    else -> ""
}

fun getShortDayOfWeekName(dayOfWeekNumber: Int, context: Context): String = when(dayOfWeekNumber){
    1 -> "Mon"
    2 -> "Tue"
    3 -> "Wen"
    4 -> "Thu"
    5 -> "Fri"
    6 -> "Sat"
    7 -> "Sun"
    else -> ""
}

fun LocalDate.toFormattedDate(context: Context): String = "${getShortDayOfWeekName(dayOfWeek.value, context)}, $dayOfMonth ${getShortMonthName(month.number, context)}"

