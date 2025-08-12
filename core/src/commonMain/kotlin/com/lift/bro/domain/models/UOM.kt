package com.lift.bro.domain.models
enum class UOM(val value: String) {
    KG("kg"),
    POUNDS("lbs")
}

fun UOM.convert(value: Double, convertTo: UOM): Double {
    if (this == convertTo) return value

    return when (this) {
        UOM.KG -> value * 0.45356237
        UOM.POUNDS -> value * 2.2046226218
    }
}