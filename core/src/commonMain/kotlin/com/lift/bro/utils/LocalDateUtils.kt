package com.lift.bro.utils

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun LocalDate.toFirstDateOfWeek(): LocalDate {
    var firstOfWeek = this
    while (firstOfWeek.dayOfWeek.ordinal != DayOfWeek.values()
            .first().ordinal
    ) {
        firstOfWeek = firstOfWeek.minus(DatePeriod(days = 1))
    }
    return firstOfWeek
}

fun LocalDate.toLastDateOfWeek(): LocalDate {
    var lastOfWeek = this
    while (lastOfWeek.dayOfWeek.ordinal != DayOfWeek.values()
            .last().ordinal
    ) {
        lastOfWeek = lastOfWeek.plus(DatePeriod(days = 1))
    }
    return lastOfWeek
}