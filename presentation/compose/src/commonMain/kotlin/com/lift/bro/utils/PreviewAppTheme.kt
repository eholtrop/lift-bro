package com.lift.bro.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.compose.AppTheme
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalCalculatorVisibility
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.JetpackComposeCoordinator
import com.lift.bro.ui.navigation.LocalNavCoordinator

@Composable
internal fun PreviewAppTheme(
    isDarkMode: Boolean,
    content: @Composable() () -> Unit
) {
    val calculatorVisibility = remember { mutableStateOf(false) }
    val subscriptionType = remember { mutableStateOf(SubscriptionType.None) }

    CompositionLocalProvider(
        LocalLiftBro provides LiftBro.Lisa,
        LocalNavCoordinator provides JetpackComposeCoordinator(Destination.Unknown),
        LocalUnitOfMeasure provides UOM.POUNDS,
        LocalCalculatorVisibility provides calculatorVisibility,
        LocalSubscriptionStatusProvider provides subscriptionType
    ) {
        AppTheme(
            theme = if (isDarkMode) ThemeMode.Dark else ThemeMode.Light
        ) {
            content()
        }
    }
}
