package com.lift.bro.utils

actual fun Double?.decimalFormat(): String {
    if (this == null) return ""

    val df = java.text.DecimalFormat()
    df.isGroupingUsed = false
    df.maximumFractionDigits = 2
    df.minimumFractionDigits = 0
    df.isDecimalSeparatorAlwaysShown = false
    return df.format(this)
}