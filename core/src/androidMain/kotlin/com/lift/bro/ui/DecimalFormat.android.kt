package com.lift.bro.utils

actual fun Double?.decimalFormat(showDecimal: Boolean): String {
    if (this == null) return ""

    val df = java.text.DecimalFormat()
    df.isGroupingUsed = false
    df.maximumFractionDigits = 3
    df.minimumFractionDigits = 0
    df.isDecimalSeparatorAlwaysShown = showDecimal
    return df.format(this)
}