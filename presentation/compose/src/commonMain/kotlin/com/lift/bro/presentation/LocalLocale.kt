package com.lift.bro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

expect object LocalLocale {
    val current: String

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
