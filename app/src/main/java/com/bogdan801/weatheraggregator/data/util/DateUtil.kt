package com.bogdan801.weatheraggregator.data.util

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import java.time.YearMonth

fun getCurrentDate(): LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date
