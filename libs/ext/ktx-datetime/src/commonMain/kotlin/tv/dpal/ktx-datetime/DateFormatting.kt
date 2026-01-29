package tv.dpal.ext.ktx.datetime

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

expect fun Instant.toString(pattern: String): String

expect fun LocalDate.toString(pattern: String): String

expect fun LocalDateTime.toString(pattern: String): String
