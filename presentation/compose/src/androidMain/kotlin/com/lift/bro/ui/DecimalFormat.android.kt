package com.lift.bro.utils

actual fun Double?.decimalFormat(
    showDecimal: Boolean,
    grouping: Boolean,
): String {
    if (this == null) return ""

    val df = java.text.DecimalFormat()
    df.isGroupingUsed = grouping
    df.maximumFractionDigits = 3
    df.minimumFractionDigits = 0
    df.isDecimalSeparatorAlwaysShown = showDecimal
    return df.format(this)
}
