package com.lift.bro.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.compose.AppTheme
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.onboarding.LiftBro
import kotlin.random.Random

@Composable
internal fun PreviewAppTheme(
    isDarkMode: Boolean,
    content: @Composable() () -> Unit
) {
    CompositionLocalProvider(LocalLiftBro provides LiftBro.Lisa) {
        AppTheme(
            useDarkTheme = isDarkMode
        ) {
            content()
        }
    }
}