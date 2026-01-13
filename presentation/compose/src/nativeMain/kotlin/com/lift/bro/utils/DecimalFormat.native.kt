package com.lift.bro.utils

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun Double?.decimalFormat(showDecimal: Boolean, grouping: Boolean): String {
    return NSNumberFormatter().apply {
        minimumFractionDigits = 0u
        maximumFractionDigits = 3u
        numberStyle = NSNumberFormatterDecimalStyle
        usesGroupingSeparator = grouping
    }.stringFromNumber(NSNumber(this ?: 0.0)) ?: ""
}

actual fun Long?.format(grouping: Boolean): String {
    return NSNumberFormatter().apply {
        usesGroupingSeparator = grouping
        numberStyle = NSNumberFormatterDecimalStyle
    }.stringFromNumber(NSNumber(longLong = this ?: 0L)) ?: ""
}

actual fun Int?.format(grouping: Boolean): String {
    return NSNumberFormatter().apply {
        usesGroupingSeparator = grouping
        numberStyle = NSNumberFormatterDecimalStyle
    }.stringFromNumber(NSNumber(this?.toInt() ?: 0)) ?: ""
}
