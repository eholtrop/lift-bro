package com.lift.bro.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter

actual fun Instant.toString(pattern: String): String {
    return NSDateFormatter().stringFromDate(this.toNSDate())
}

actual fun LocalDate.toString(pattern: String): String {
    return NSDateFormatter().apply {
        this.dateFormat = pattern
    }.stringFromDate(this.atStartOfDayIn(TimeZone.currentSystemDefault()).toNSDate())
}

actual fun LocalDateTime.toString(pattern: String): String {
    return NSDateFormatter().stringFromDate(this.toInstant(TimeZone.currentSystemDefault()).toNSDate())
}