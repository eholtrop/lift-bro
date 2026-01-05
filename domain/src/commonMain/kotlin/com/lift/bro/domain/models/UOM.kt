package com.lift.bro.domain.models

enum class UOM(val value: String) {
    KG("kg"),
    POUNDS("lbs")
}

fun UOM.convert(value: Double, convertTo: UOM): Double {
    if (this == convertTo) return value

    return when (this) {
        UOM.KG -> value * LBS_TO_KG_COEFFICIENT
        UOM.POUNDS -> value * KG_TO_LBS_COEFFICIENT
    }
}

private const val KG_TO_LBS_COEFFICIENT = 2.2046226218
private const val LBS_TO_KG_COEFFICIENT = 0.45356237
