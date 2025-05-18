package com.lift.bro.ui

actual object DecimalFormat {
    actual fun formatWeight(weight: Double?): String {
        return weight.toString()
    }
}