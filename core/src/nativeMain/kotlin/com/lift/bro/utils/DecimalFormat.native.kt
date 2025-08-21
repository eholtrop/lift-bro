package com.lift.bro.utils

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun Double?.decimalFormat(showDecimal: Boolean): String {
    return NSNumberFormatter().apply {
        minimumFractionDigits = 0u
        maximumFractionDigits = 3u
        numberStyle = NSNumberFormatterDecimalStyle
    }.stringFromNumber(NSNumber(this ?: 0.0)) ?: ""
}