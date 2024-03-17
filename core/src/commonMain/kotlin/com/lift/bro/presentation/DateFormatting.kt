package com.lift.bro.presentation

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect fun Instant.toString(pattern: String): String

expect fun LocalDate.toString(pattern: String): String
