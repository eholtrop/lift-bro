package com.lift.bro.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.usecases.GetCelebrationTypeUseCase
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.presentation.onboarding.LiftBro
import com.lift.bro.ui.ConfettiExplosion
import com.lift.bro.ui.LiftCardYValue
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.BackupAlertDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.ui.navigation.SwipeableNavHost
import com.lift.bro.ui.navigation.rememberNavCoordinator
import com.lift.bro.ui.theme.spacing
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
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

val LocalLiftCardYValue = compositionLocalOf<MutableState<LiftCardYValue>> {
    error("Lift Card Y Value was not set")
}

val LocalAdBannerProvider = compositionLocalOf<() -> Any> {
    error("No Ad Banner Provided")
}

@Composable
fun App(
    modifier: Modifier = Modifier,
    navCoordinator: NavCoordinator = rememberNavCoordinator(Destination.Onboarding)
) {

    val bro by dependencies.settingsRepository.getBro().collectAsState(null)
    val uom by dependencies.settingsRepository.getUnitOfMeasure().map { it.uom }
        .collectAsState(UOM.POUNDS)
    val showMerCalcs by dependencies.settingsRepository.getMerSettings().collectAsState(null)

    CompositionLocalProvider(
        LocalLiftBro provides (bro ?: if (Random.nextBoolean()) LiftBro.Leo else LiftBro.Lisa),
        LocalUnitOfMeasure provides uom,
        LocalShowMERCalcs provides showMerCalcs,
        LocalLiftCardYValue provides mutableStateOf(LiftCardYValue.Weight)
    ) {
        LaunchedEffect("landing_selection") {
            dependencies.settingsRepository.getDeviceFtux().collectLatest {
                when (it) {
                    true -> navCoordinator.setRoot(Destination.Dashboard)
                    false -> navCoordinator.setRoot(Destination.Onboarding)
                }
            }
        }

        LaunchedEffect("initialize_sentry") {
            Sentry.init { options ->
                options.dsn = BuildKonfig.SENTRY_DSN
            }
        }

        AppTheme {
            Box(
                modifier = modifier,
            ) {
                LaunchedEffect("debug_mode") {
                    if (BuildConfig.isDebug) {
                    }
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

                // This is all pretty terrible.... but its something I promised a friend id release before they hit PR!... and I got bugs to fix
                var celebration by remember { mutableStateOf<CelebrationType>(CelebrationType.None) }

                LaunchedEffect(Unit) {
                    GetCelebrationTypeUseCase()
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
                                            CelebrationType.FirstLift -> "That's a new Personal Record!"
                                            CelebrationType.FirstVariation -> "That's a new Personal Record!"
                                            is CelebrationType.NewLiftMax -> "That's a new ${cel.liftName} Personal Record!"
                                            is CelebrationType.NewVariationMax -> "That's a new ${cel.variationName} Personal Record!"
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
