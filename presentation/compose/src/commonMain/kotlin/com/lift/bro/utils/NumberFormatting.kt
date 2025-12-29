package com.lift.bro.utils

expect fun Double?.decimalFormat(showDecimal: Boolean = false, grouping: Boolean = false): String

expect fun Long?.format(grouping: Boolean = true): String

expect fun Int?.format(grouping: Boolean = true): String

fun Double?.percentageFormat(): String {
    if (this == null) return ""
    return "${(this * 100).toInt()}%"
}
