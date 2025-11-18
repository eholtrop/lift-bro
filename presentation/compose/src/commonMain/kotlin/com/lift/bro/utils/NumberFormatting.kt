package com.lift.bro.utils

expect fun Double?.decimalFormat(showDecimal: Boolean = false, grouping: Boolean = false): String

fun Double?.percentageFormat(): String {
    if (this == null) return ""
    return "${(this * 100).toInt()}%"
}
