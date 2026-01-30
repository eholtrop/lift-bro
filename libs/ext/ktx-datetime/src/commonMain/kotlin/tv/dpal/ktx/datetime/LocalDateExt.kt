package tv.dpal.ktx.datetime

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

fun LocalDate.atStartOfDayIn(): Instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())

fun LocalDate.atEndOfDayIn(): Instant =
    this.atTime(23, 59, 59, 999999999).toInstant(TimeZone.currentSystemDefault())
