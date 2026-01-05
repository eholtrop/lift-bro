package com.lift.bro.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.compose.AppTheme
import com.lift.bro.di.DependencyContainer
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalCalculatorVisibility
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.JetpackComposeCoordinator
import com.lift.bro.ui.navigation.LocalNavCoordinator

@Composable
internal fun PreviewAppTheme(
    isDarkMode: Boolean,
    content:
    @Composable()
    () -> Unit
) {
    DependencyContainer.initialize(LocalContext.current)
    CompositionLocalProvider(
        LocalLiftBro provides LiftBro.Lisa,
        LocalNavCoordinator provides JetpackComposeCoordinator(Destination.Unknown),
        LocalUnitOfMeasure provides UOM.POUNDS,
        LocalCalculatorVisibility provides mutableStateOf(false)
    ) {
        AppTheme(
            theme = if (isDarkMode) ThemeMode.Dark else ThemeMode.Light
        ) {
            content()
        }
    }
}
