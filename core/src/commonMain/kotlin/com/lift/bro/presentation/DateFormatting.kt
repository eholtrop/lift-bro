package com.lift.bro.presentation

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.formatDate(pattern: String): String = this.toLocalDateTime(TimeZone.UTC).date.let {
    "${it.year}-${it.monthNumber}-${it.dayOfMonth}"
}
