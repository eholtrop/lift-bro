package com.lift.bro.presentation

actual fun Double.decimalFormat(): String {
    val df = java.text.DecimalFormat()
    df.isGroupingUsed = false
    df.maximumFractionDigits = 2
    df.minimumFractionDigits = 0
    df.isDecimalSeparatorAlwaysShown = false
    return df.format(this)
}