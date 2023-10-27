package com.lift.bro.ui

import android.icu.text.DecimalFormat

actual object DecimalFormat {
    actual fun formatWeight(weight: Double?): String {
        return try {
            DecimalFormat("#.##").format(weight)
        } catch(exception: Exception) {
            ""
        }
    }
}