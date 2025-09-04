package com.lift.bro.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.lift.bro.AppRouter
import com.lift.bro.config.BuildConfig
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.CelebrationType
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.usecases.ConsentDeviceUseCase
import com.lift.bro.domain.usecases.GetCelebrationTypeUseCase
import com.lift.bro.domain.usecases.HasDeviceConsentedUseCase
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.presentation.onboarding.LiftBro
import com.lift.bro.ui.ConfettiExplosion
import com.lift.bro.ui.ConsentCheckBoxField
import com.lift.bro.ui.LiftCardYValue
import com.lift.bro.ui.Space
import com.lift.bro.ui.calculator.WeightCalculatorBottomSheet
import com.lift.bro.ui.dialog.BackupAlertDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.ui.navigation.SwipeableNavHost
import com.lift.bro.ui.navigation.rememberNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.debug
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.consent_dialog_cta
import lift_bro.core.generated.resources.consent_dialog_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random


val LocalLiftBro = compositionLocalOf<LiftBro> {
    error("LiftBro was not set")
}

val LocalUnitOfMeasure = compositionLocalOf<UOM> {
    error("UOM was not set")
}

val LocalShowMERCalcs = compositionLocalOf<MERSettings?> {
    error("Show MER Calcs was not set")
}

val LocalTwmSettings = compositionLocalOf<Boolean> {
    error("Show MER Calcs was not set")
}

val LocalEMaxSettings = compositionLocalOf<Boolean> {
    error("Show eMax was not set")
}

val LocalPlatformContext = compositionLocalOf<Any?> {
}

val LocalTMaxSettings = compositionLocalOf<Boolean> {
    error("Show tMax was not set")
}

val LocalLiftCardYValue = compositionLocalOf<MutableState<LiftCardYValue>> {
    error("Lift Card Y Value was not set")
}

val LocalAdBannerProvider = compositionLocalOf<() -> Any> {
    error("No Ad Banner Provided")
}

val LocalSubscriptionStatusProvider = compositionLocalOf<MutableState<SubscriptionType>> {
    error("No Subscription Provided")
}

val LocalPaywallVisibility = compositionLocalOf<MutableState<Boolean>> {
    error("No Subscription Provided")
}

val LocalCalculatorVisibility = compositionLocalOf<MutableState<Boolean>> {
    error("No Calculator Visibility Provided")
}

@Composable
fun CheckAppConsent() {
    val hasConsent by HasDeviceConsentedUseCase().invoke().collectAsState(null)

    if (hasConsent == false) {
        var accepted by remember { mutableStateOf(false) }
        AlertDialog(
            title = { Text(stringResource(Res.string.consent_dialog_title)) },
            text = {
                ConsentCheckBoxField(
                    accepted = accepted,
                    acceptanceChanged = { accepted = it }
                )
            },
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = {
                        ConsentDeviceUseCase().invoke()
                    },
                    enabled = accepted
                ) {
                    Text(stringResource(Res.string.consent_dialog_cta))
                }
            },
        )
    }
}

@Composable
fun App(
    modifier: Modifier = Modifier,
    navCoordinator: NavCoordinator = rememberNavCoordinator(Destination.Onboarding)
) {

    val subscriptionType = remember { mutableStateOf(SubscriptionType.None) }
    val isAndroid = LocalPlatformContext.current != null

    LaunchedEffect("setup_revenuecat") {
        if (BuildConfig.isDebug) {
            Purchases.logLevel = LogLevel.DEBUG
        }

        Log.d("", isAndroid.toString())
        Purchases.configure(if (isAndroid) BuildKonfig.REVENUE_CAT_API_KEY_AND else BuildKonfig.REVENUE_CAT_API_KEY_IOS)
        Purchases.sharedInstance.getCustomerInfo(
            onError = { error ->
                Sentry.captureException(Throwable(message = error.message))
            },
            onSuccess = { success ->
                if (success.entitlements.active.containsKey("pro")) {
                    subscriptionType.value = SubscriptionType.Pro
                }
            }
        )
    }

    val bro by dependencies.settingsRepository.getBro().collectAsState(null)
    val uom by dependencies.settingsRepository.getUnitOfMeasure().map { it.uom }
        .collectAsState(UOM.POUNDS)
    val showMerCalcs by dependencies.settingsRepository.getMerSettings().collectAsState(null)
    val twmSettings by dependencies.settingsRepository.shouldShowTotalWeightMoved()
        .collectAsState(false)
    val emaxSettings by dependencies.settingsRepository.eMaxEnabled().collectAsState(false)
    val tMaxSettings by dependencies.settingsRepository.tMaxEnabled().collectAsState(false)
    val showPaywall = remember { mutableStateOf(false) }
    val showCalculator = remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalLiftBro provides (bro ?: if (Random.nextBoolean()) LiftBro.Leo else LiftBro.Lisa),
        LocalUnitOfMeasure provides uom,
        LocalShowMERCalcs provides showMerCalcs,
        LocalTwmSettings provides (twmSettings && (subscriptionType.value == SubscriptionType.Pro || BuildConfig.isDebug)),
        LocalEMaxSettings provides (emaxSettings && (subscriptionType.value == SubscriptionType.Pro || BuildConfig.isDebug)),
        LocalTMaxSettings provides (tMaxSettings && (subscriptionType.value == SubscriptionType.Pro || BuildConfig.isDebug)),
        LocalLiftCardYValue provides mutableStateOf(LiftCardYValue.Weight),
        LocalSubscriptionStatusProvider provides subscriptionType,
        LocalPaywallVisibility provides showPaywall,
        LocalCalculatorVisibility provides showCalculator
    ) {
        LaunchedEffect("landing_selection") {
            dependencies.settingsRepository.getDeviceFtux().collectLatest {
                when (it) {
                    true -> navCoordinator.setRoot(Destination.Home)
                    false -> navCoordinator.setRoot(Destination.Onboarding)
                }
            }
        }


        val context = LocalPlatformContext.current
        LaunchedEffect("initialize_sentry") {
            // keep sentry until we know firebase is working
            if (!BuildConfig.isDebug) {
                Sentry.init { options ->
                    options.dsn = BuildKonfig.SENTRY_DSN
                }
            }

            if (!BuildConfig.isDebug) {
                Firebase.initialize(context)
            }
        }


        AppTheme {
            Box(
                modifier = modifier,
            ) {

                if (navCoordinator.currentPage != Destination.Onboarding) {
                    CheckAppConsent()
                }
                BackupAlertDialog()

                Column {
                    SwipeableNavHost(
                        modifier = Modifier.weight(1f),
                        navCoordinator = navCoordinator,
                    ) { route ->
                        AppRouter(route)
                    }
                }


                val options = remember {
                    PaywallOptions(dismissRequest = { showPaywall.value = false }) {
                        shouldDisplayDismissButton = true
                    }
                }

                AnimatedVisibility(
                    visible = showPaywall.value,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Paywall(options)
                }

                WeightCalculatorBottomSheet()

                // This is all pretty terrible.... but its something I promised a friend id release before they hit PR!... and I got bugs to fix
                var celebration by remember { mutableStateOf<CelebrationType>(CelebrationType.None) }

                LaunchedEffect(Unit) {
                    GetCelebrationTypeUseCase()
                        .debug("DEBUGEH")
                        .collectLatest {
                            celebration = it
                        }
                }

                if (celebration != CelebrationType.None) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ConfettiExplosion()

                        var showMessage by remember { mutableStateOf(false) }
                        AnimatedVisibility(
                            modifier = Modifier.align(Alignment.Center),
                            visible = showMessage,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut(),
                        ) {
                            val speechBubbleColor = MaterialTheme.colorScheme.primary
                            Column {
                                Column(
                                    modifier = Modifier
                                        .semantics(
                                            mergeDescendants = true,
                                        ) {
                                            liveRegion = LiveRegionMode.Assertive
                                        }
                                        .background(
                                            speechBubbleColor,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                        .padding(MaterialTheme.spacing.one),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = "Congrats!!",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                    Space(MaterialTheme.spacing.half)
                                    Text(
                                        text = when (val cel = celebration) {
                                            is CelebrationType.NewEMax -> "New estimated Max!"
                                            is CelebrationType.NewOneRepMax -> "New one rep max!"
                                            CelebrationType.None -> ""
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Row {
                                    Image(
                                        modifier = Modifier
                                            .size(72.dp),
                                        painter = painterResource(LocalLiftBro.current.iconRes()),
                                        contentDescription = ""
                                    )
                                    Canvas(
                                        modifier = Modifier.size(72.dp.div(2))
                                    ) {
                                        drawPath(
                                            Path().apply {
                                                moveTo(20f, 0f)
                                                lineTo(0f, 60f)
                                                lineTo(60f, 0f)
                                                lineTo(0f, 0f)
                                                close()
                                            },
                                            speechBubbleColor
                                        )
                                    }
                                }
                            }
                        }

                        LaunchedEffect(Unit) {
                            delay(1000)
                            showMessage = true
                            delay(4000)
                            showMessage = false
                            delay(2000)
                            celebration = CelebrationType.None
                        }
                    }
                }
            }
        }
    }
}
