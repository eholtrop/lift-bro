package com.lift.bro.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

private fun dateFormatter(pattern: String) = NSDateFormatter().apply {
    this.dateFormat = pattern
    this.locale = NSLocale.currentLocale
}

actual fun Instant.toString(pattern: String): String {
    return dateFormatter(pattern).stringFromDate(this.toNSDate())
}

actual fun LocalDate.toString(pattern: String): String {
    return dateFormatter(pattern).stringFromDate(this.atStartOfDayIn(TimeZone.currentSystemDefault()).toNSDate())
}

actual fun LocalDateTime.toString(pattern: String): String {
    return dateFormatter(pattern).stringFromDate(this.toInstant(TimeZone.currentSystemDefault()).toNSDate())
}
