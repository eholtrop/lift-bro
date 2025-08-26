package com.lift.bro.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.example.compose.amber
import com.lift.bro.BackupService
import com.lift.bro.di.dependencies
import com.lift.bro.domain.usecases.ConsentDeviceUseCase
import com.lift.bro.ui.Card
import com.lift.bro.ui.ConsentCheckBoxField
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.ic_lift_bro_leo_light
import lift_bro.core.generated.resources.ic_lift_bro_lisa_concerned_light
import lift_bro.core.generated.resources.onboarding_consent_screen_subtitle
import lift_bro.core.generated.resources.onboarding_consent_screen_title
import lift_bro.core.generated.resources.onboarding_leo_content_description
import lift_bro.core.generated.resources.onboarding_lisa_content_description
import lift_bro.core.generated.resources.onboarding_page_one_cta
import lift_bro.core.generated.resources.onboarding_page_one_title
import lift_bro.core.generated.resources.onboarding_skip_screen_subtitle
import lift_bro.core.generated.resources.onboarding_skip_screen_title
import lift_bro.core.generated.resources.privacy_policy
import lift_bro.core.generated.resources.terms_and_conditions
import lift_bro.core.generated.resources.url_privacy_policy
import lift_bro.core.generated.resources.url_terms_and_conditions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Modifier.onboardingBackground(): Modifier = this
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondary,
                amber,
            ),
        )
    )

enum class LiftBro {
    Leo, Lisa
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OnboardingScreen(
    defaultState: Int = 0
) {
    var onboardingState by remember { mutableStateOf(defaultState) }

    AppTheme(
        useDarkTheme = false,
    ) {

        AnimatedContent(
            onboardingState,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90))
                    .togetherWith(fadeOut(animationSpec = tween(90))))
            }
        ) { state ->
            when (state) {
                0 -> OnboardingBroScreen {
                    dependencies.settingsRepository.setBro(it)
                    onboardingState += 1
                }

                1 -> OnboardingConsentScreen {
                    ConsentDeviceUseCase().invoke()
                    onboardingState += 1
                }
                2 -> OnboardingSkipScreen(
                    setupClicked = { onboardingState += 1 },
                    continueClicked = { dependencies.settingsRepository.setDeviceFtux(true) }
                )

                3 -> OnboardingSetupScreen { dependencies.settingsRepository.setDeviceFtux(true) }
            }
        }

        BackHandler(enabled = onboardingState != 0) {
            onboardingState -= 1
        }
    }

}

@Composable
fun OnboardingBroScreen(
    broSelected: (LiftBro) -> Unit,
) {
    var bro by remember { mutableStateOf<LiftBro?>(null) }

    Column(
        modifier = Modifier
            .onboardingBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.two,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.onboarding_page_one_title),
            style = MaterialTheme.typography.displayLarge
        )

        Space()

        val leoContentDescription = stringResource(Res.string.onboarding_leo_content_description)
        Card(
            modifier = Modifier.fillMaxWidth(.5f)
                .aspectRatio(1f)
                .semantics(mergeDescendants = true) {
                    contentDescription = leoContentDescription
                }
                .border(
                    width = if (bro == LiftBro.Leo) 4.dp else 0.dp,
                    color = Color.Black,
                    shape = MaterialTheme.shapes.medium
                ),
            backgroundColor = Color.White,
            onClick = { bro = LiftBro.Leo },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.weight(1f),
                    painter = painterResource(Res.drawable.ic_lift_bro_leo_light),
                    contentDescription = null
                )
            }
        }

        val lisaContentDescription = stringResource(Res.string.onboarding_lisa_content_description)
        Card(
            modifier = Modifier.fillMaxWidth(.5f)
                .aspectRatio(1f)
                .semantics(mergeDescendants = true) {
                    contentDescription = lisaContentDescription
                }
                .border(
                    width = if (bro == LiftBro.Lisa) 4.dp else 0.dp,
                    color = Color.Black,
                    shape = MaterialTheme.shapes.medium
                ),
            backgroundColor = Color.White,
            onClick = { bro = LiftBro.Lisa },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.weight(1f),
                    painter = painterResource(Res.drawable.ic_lift_bro_lisa_concerned_light),
                    contentDescription = null
                )
            }
        }

        Space()

        Button(
            modifier = Modifier.height(Dp.AccessibilityMinimumSize),
            onClick = { broSelected(bro!!) },
            colors = ButtonDefaults.elevatedButtonColors(
                contentColor = Color.Black
            ),
            enabled = bro != null
        ) {
            Text(
                stringResource(Res.string.onboarding_page_one_cta),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun OnboardingConsentScreen(
    consentAccepted: () -> Unit,
) {
    Column(
        modifier = Modifier
            .onboardingBackground()
            .navigationBarsPadding()
            .statusBarsPadding()
            .padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.two,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.onboarding_consent_screen_title),
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.onboarding_consent_screen_subtitle),
            style = MaterialTheme.typography.titleLarge
        )

        Space()

        val termsUrl = stringResource(Res.string.url_terms_and_conditions)
        Button(
            modifier = Modifier
                .height(Dp.AccessibilityMinimumSize),
            onClick = {
                dependencies.launchUrl(termsUrl)
            },
            colors = ButtonDefaults.elevatedButtonColors(),
        ) {
            Text(
                text = stringResource(Res.string.terms_and_conditions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
            )
        }

        val privacyUrl = stringResource(Res.string.url_privacy_policy)
        Button(
            modifier = Modifier
                .height(Dp.AccessibilityMinimumSize),
            onClick = {
                dependencies.launchUrl(privacyUrl)
            },
            colors = ButtonDefaults.elevatedButtonColors(),
        ) {
            Text(
                text = stringResource(Res.string.privacy_policy),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
            )
        }

        Space()

        var accepted by remember { mutableStateOf(false) }

        ConsentCheckBoxField(
            accepted = accepted,
            acceptanceChanged = { accepted = it }
        )

        Space(MaterialTheme.spacing.half)

        Button(
            modifier = Modifier.height(Dp.AccessibilityMinimumSize),
            onClick = { consentAccepted() },
            colors = ButtonDefaults.elevatedButtonColors(
                contentColor = Color.Black
            ),
            enabled = accepted
        ) {
            Text(
                stringResource(Res.string.onboarding_page_one_cta),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun OnboardingSkipScreen(
    setupClicked: () -> Unit,
    continueClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .onboardingBackground()
            .navigationBarsPadding()
            .statusBarsPadding()
            .fillMaxSize()
            .padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.two,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.onboarding_skip_screen_title),
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.onboarding_skip_screen_subtitle),
            style = MaterialTheme.typography.titleLarge
        )

        Space(MaterialTheme.spacing.one.times(3))

        Button(
            modifier = Modifier
                .height(Dp.AccessibilityMinimumSize),
            onClick = { setupClicked() },
            colors = ButtonDefaults.elevatedButtonColors(),
        ) {
            Text(
                text = "\uD83D\uDE4F Help me setup some Lifts",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
            )
        }

        val coroutineScope = rememberCoroutineScope()
        Button(
            modifier = Modifier
                .height(Dp.AccessibilityMinimumSize),
            onClick = {
                coroutineScope.launch {
                    if (BackupService.restore()) {
                        continueClicked()
                    }
                }
            },
            colors = ButtonDefaults.elevatedButtonColors(),
        ) {
            Text(
                text = "\uD83E\uDDBA Restore from a Backup",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
            )
        }
        Button(
            modifier = Modifier
                .height(Dp.AccessibilityMinimumSize),
            onClick = { continueClicked() },
            colors = ButtonDefaults.elevatedButtonColors(),
        ) {
            Text(
                text = "\uD83E\uDEE1 Just put me in Coach!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
            )
        }

        Space()
    }
}
