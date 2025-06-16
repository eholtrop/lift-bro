package com.lift.bro.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.compose.AppTheme
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.onboarding.LiftBro
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.JetpackComposeCoordinator
import com.lift.bro.ui.navigation.LocalNavCoordinator
import kotlin.random.Random

@Composable
internal fun PreviewAppTheme(
    isDarkMode: Boolean,
    content: @Composable() () -> Unit
) {
    CompositionLocalProvider(
        LocalLiftBro provides LiftBro.Lisa,
        LocalNavCoordinator provides JetpackComposeCoordinator(Destination.Unknown),
        LocalUnitOfMeasure provides UOM.POUNDS
    ) {
        AppTheme(
            useDarkTheme = isDarkMode
        ) {
            content()
        }
    }
}