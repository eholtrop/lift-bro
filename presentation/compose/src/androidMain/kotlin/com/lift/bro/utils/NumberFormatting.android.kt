package com.lift.bro.utils

import java.text.NumberFormat
import java.util.Locale


actual fun Long?.format(grouping: Boolean): String {
    return NumberFormat.getInstance(Locale.getDefault()).let {
        it.isGroupingUsed = grouping
        it.format(this)
    }
}

actual fun Int?.format(grouping: Boolean): String {
    return NumberFormat.getInstance(Locale.getDefault()).let {
        it.isGroupingUsed = grouping
        it.format(this)
    }
}

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
