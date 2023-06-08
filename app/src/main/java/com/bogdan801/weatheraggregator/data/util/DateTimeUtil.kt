package com.bogdan801.weatheraggregator.data.util

import android.content.Context
import androidx.core.text.isDigitsOnly
import com.bogdan801.weatheraggregator.R
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
    1 ->  context.getString(R.string.jan)
    2 ->  context.getString(R.string.feb)
    3 ->  context.getString(R.string.mar)
    4 ->  context.getString(R.string.apr)
    5 ->  context.getString(R.string.may)
    6 ->  context.getString(R.string.jun)
    7 ->  context.getString(R.string.jul)
    8 ->  context.getString(R.string.aug)
    9 ->  context.getString(R.string.sep)
    10 -> context.getString(R.string.oct)
    11 -> context.getString(R.string.nov)
    12 -> context.getString(R.string.dec)
    else -> ""
}

fun getShortDayOfWeekName(dayOfWeekNumber: Int, context: Context): String = when(dayOfWeekNumber){
    1 -> context.getString(R.string.mon)
    2 -> context.getString(R.string.tue)
    3 -> context.getString(R.string.wen)
    4 -> context.getString(R.string.thu)
    5 -> context.getString(R.string.fri)
    6 -> context.getString(R.string.sat)
    7 -> context.getString(R.string.sun)
    else -> ""
}

fun LocalDate.toFormattedDate(context: Context): String = "${getShortDayOfWeekName(dayOfWeek.value, context)}, $dayOfMonth ${getShortMonthName(month.number, context)}"

