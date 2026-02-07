package com.lift.bro.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.compose.AppTheme
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalAdBannerProvider
import com.lift.bro.presentation.LocalCalculatorVisibility
import com.lift.bro.presentation.LocalEMaxSettings
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalPaywallVisibility
import com.lift.bro.presentation.LocalPlatformContext
import com.lift.bro.presentation.LocalServer
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.LocalTMaxSettings
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.card.lift.LiftCardYValue
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import tv.dpal.navi.JetpackComposeCoordinator
import tv.dpal.navi.LocalNavCoordinator

@Composable
internal fun PreviewAppTheme(
    isDarkMode: Boolean,
    content:
    @Composable()
    () -> Unit,
) {
    val calculatorVisibility = remember { mutableStateOf(false) }
    val subscriptionType = remember { mutableStateOf(SubscriptionType.None) }

    CompositionLocalProvider(
        LocalLiftBro provides LiftBro.Lisa,
        LocalNavCoordinator provides JetpackComposeCoordinator(Destination.Unknown),
        LocalUnitOfMeasure provides UOM.POUNDS,
        LocalTwmSettings provides true,
        LocalShowMERCalcs provides MERSettings(enabled = true),
        LocalCalculatorVisibility provides calculatorVisibility,
        LocalSubscriptionStatusProvider provides subscriptionType,
        LocalLiftCardYValue provides mutableStateOf(LiftCardYValue.Weight),
        LocalPlatformContext provides null,
        LocalServer provides null,
        LocalEMaxSettings provides true,
        LocalTMaxSettings provides true,
        LocalAdBannerProvider provides {},
        LocalPaywallVisibility provides mutableStateOf(false)
    ) {
        AppTheme(
            theme = if (isDarkMode) ThemeMode.Dark else ThemeMode.Light
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                content()
            }
        }
    }
}

class DarkModeProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(true, false)
}
