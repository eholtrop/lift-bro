package com.lift.bro.domain.models

import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d

enum class UOM(val value: String) {
    KG("kg"),
    POUNDS("lbs")
}

fun UOM.convert(value: Double, convertTo: UOM): Double {
    if (this == convertTo) return value

    return when (this) {
        UOM.KG -> value * 2.2046226218
        UOM.POUNDS -> value * 0.45356237
    }
}