package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lift.bro.data.core.livestream.LiveStreamRepository
import com.lift.bro.di.dependencies
import com.lift.bro.di.liveStreamRepository
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.Setting
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.home_screen_icon_live_dialog_text
import lift_bro.core.generated.resources.home_screen_icon_live_dialog_title
import lift_bro.core.generated.resources.home_screen_icon_live_label
import lift_bro.core.generated.resources.home_screen_icon_maybe_later_cta
import lift_bro.core.generated.resources.home_screen_icon_tune_in_cta
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.rememberInteractor

@Composable
fun HomeScreenIcon(
    modifier: Modifier = Modifier,
    interactor: Interactor<HomeScreenIconState, Nothing> = rememberHomeScreenIconInteractor(),
) {
    val state by interactor.state.collectAsState()
    var size by remember { mutableStateOf(Size.Zero) }
    var showConfirmationModal by remember { mutableStateOf(false) }

    if (showConfirmationModal) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationModal = false
            },
            title = {
               Text(
                   text = stringResource(Res.string.home_screen_icon_live_dialog_title)
               )
            },
            text = {
               Text(
                   text = stringResource(Res.string.home_screen_icon_live_dialog_text)
               )
            },
            confirmButton = {
                Button(
                    onClick =  {
                        dependencies.launchUrl("https://twitch.tv/dpaltv")
                        showConfirmationModal = false
                    }
                ) {
                    Text(stringResource(Res.string.home_screen_icon_tune_in_cta))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showConfirmationModal = false
                    }
                ) {
                    Text(stringResource(Res.string.home_screen_icon_maybe_later_cta))
                }
            },
        )
    }

    Box(
        modifier = modifier
            .clickable(
                enabled = state.live,
                onClick = {
                    showConfirmationModal = true
                },
                role = Role.Button,
            )
            .height(
                if (state.live) 72.dp else 52.dp
            )
            .graphicsLayer {
                size = this.size
            },
    ) {
        Icon(
            modifier = Modifier,
            painter = painterResource(
                state.bro.iconRes()
            ),
            contentDescription = "",
        )
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(top = (size.height * .20).dp),
            visible = state.live,
            enter = fadeIn(),
        ) {
            Box {
                Text(
                    text = stringResource(Res.string.home_screen_icon_live_label),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.home_screen_icon_live_label),
                    style = MaterialTheme.typography.headlineSmall
                        .copy(
                            drawStyle = Stroke(
                                miter = 10f,
                                width = 5f,
                            ),
                        ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}

@Serializable
data class HomeScreenIconState(
    val live: Boolean,
    val bro: LiftBro,
)

@Composable
fun rememberHomeScreenIconInteractor(
    settingsRepository: ISettingsRepository = dependencies.settingsRepository,
    liveStreamRepositry: LiveStreamRepository = dependencies.liveStreamRepository,
) = rememberInteractor<HomeScreenIconState, Nothing>(
    initialState = HomeScreenIconState(
        false,
        bro = LiftBro.Lisa,
    ),
    source = {
        combine(
            settingsRepository.listen(Setting.Bro),
            liveStreamRepositry.isLive("https://twitch.tv/dpaltv"),
        ) { bro, live ->
            HomeScreenIconState(
                bro = bro,
                live = live
            )
        }
    }
)
