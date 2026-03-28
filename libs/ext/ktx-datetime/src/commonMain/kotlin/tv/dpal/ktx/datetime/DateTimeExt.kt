package tv.dpal.ktx.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val today: LocalDate
    get() {
        val now = kotlin.time.Clock.System.now()
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
