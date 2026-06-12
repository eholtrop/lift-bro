package com.lift.bro.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.intl.Locale
import androidx.core.os.LocaleListCompat

actual object LocalLocale {

    actual val current: String
        @Composable
        get() = Locale.current.toLanguageTag()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val override = value ?: current

        LaunchedEffect(value) {
            when (value) {
                null -> LocaleListCompat.getEmptyLocaleList()
                else -> LocaleListCompat.forLanguageTags(value)
            }.also {
                AppCompatDelegate.setApplicationLocales(it)
            }
        }
        java.util.Locale.setDefault(java.util.Locale.forLanguageTag(override))

        return with(LocalConfiguration.current) {
            setLocale(java.util.Locale.forLanguageTag(override))
            LocalConfiguration.provides(LocalConfiguration.current)
        }
    }
}
