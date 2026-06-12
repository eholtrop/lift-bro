package com.lift.bro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.text.intl.Locale

actual object LocalLocale {
    actual val current: String
        @Composable
        get() = Locale.current.language

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        TODO()
    }
}
