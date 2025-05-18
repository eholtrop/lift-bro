package com.lift.bro.utils

import com.lift.bro.ui.DecimalFormat

actual fun Double.decimalFormat(): String {
    return this.toString()
}