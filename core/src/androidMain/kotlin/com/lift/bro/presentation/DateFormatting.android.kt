package com.lift.bro.presentation

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import java.text.SimpleDateFormat
import java.util.Date

actual fun Instant.toString(pattern: String): String {
    return SimpleDateFormat(pattern).format(Date(this.toEpochMilliseconds()))
}

actual fun LocalDate.toString(pattern: String): String {
    return SimpleDateFormat(pattern).format(Date(this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()))
}