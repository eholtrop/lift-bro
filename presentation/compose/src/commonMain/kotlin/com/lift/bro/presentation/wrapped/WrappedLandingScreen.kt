@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.wrapped

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.wrapped.goals.WrappedGoalsScreen
import com.lift.bro.presentation.wrapped.goals.rememberWrappedGoalsInteractor
import com.lift.bro.presentation.wrapped.summary.WrappedSummaryScreen
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.wrapped_landing_back_button_content_description
import lift_bro.core.generated.resources.wrapped_landing_next_button_content_description
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter


@Composable
@Preview
fun WrappedLandingScreenPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(darkMode) {
        WrappedLandingScreen(
            state = WrappedState(
                listOf(
                    WrappedPageState.Tenure
                )
            ),
            onClosePressed = {}
        )
    }
}

@Serializable
data class WrappedState(
    val pages: List<WrappedPageState> = emptyList(),
)

@Serializable
sealed class WrappedPageState() {
    @Serializable
    data object Tenure: WrappedPageState()

    @Serializable
    data object Weight: WrappedPageState()

    @Serializable
    data class Reps(
        val totalReps: Long,
        val dailyAverage: Long,
        val workoutAverage: Long,
        val mostRepsLift: Pair<String, Long>,
    ): WrappedPageState()

    @Serializable
    data class ProgressItemWeight(
        val date: LocalDate,
        val weight: Double,
        val reps: Long,
    )

    @Serializable
    data class ProgressItemState(
        val title: String,
        val minWeight: ProgressItemWeight?,
        val maxWeight: ProgressItemWeight?,
        val progress: Double,
    )

    @Serializable
    data class Progress(
        val items: List<ProgressItemState>,
    ): WrappedPageState()

    @Serializable
    data class Consistency(
        val dates: Set<LocalDate>,
    ): WrappedPageState()

    @Serializable
    data object Goals: WrappedPageState()

    @Serializable
    data class Summary(
        val sets: List<LBSet>,
    ): WrappedPageState()
}

sealed class WrappedEvents()


@Composable
fun WrappedLandingScreen(
    interactor: Interactor<WrappedState, WrappedEvents> = rememberWrappedInteractor(),
    onClosePressed: () -> Unit,
) {
    val state by interactor.state.collectAsState()

    WrappedLandingScreen(
        state,
        onClosePressed = onClosePressed,
    )
}

@Composable
fun WrappedLandingScreen(
    state: WrappedState,
    onClosePressed: () -> Unit,
) {
    val pagerState = rememberPagerState { state.pages.size }

    Column {
        Row(
            modifier = Modifier.height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val coroutineScope = rememberCoroutineScope()

            if (pagerState.currentPage == 0) {
                IconButton(
                    onClick = onClosePressed
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.wrapped_landing_back_button_content_description)
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pagerState.pageCount) {
                    Box(
                        modifier = Modifier.alpha(if (pagerState.currentPage == it) 1f else .5f)
                            .background(
                                color = if (pagerState.currentPage == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .size(if (pagerState.currentPage == it) 12.dp else 8.dp)
                    ) {

                    }
                }
            }

            if (pagerState.currentPage == state.pages.lastIndex) {
                IconButton(
                    onClick = onClosePressed
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(Res.string.wrapped_landing_next_button_content_description)
                    )
                }
            }
        }

        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState,
        ) { page ->
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onBackground
            ) {

                when (val page = state.pages[page]) {
                    is WrappedPageState.Reps -> WrappedRepScreen()
                    is WrappedPageState.Tenure -> WrappedTenureScreen()
                    is WrappedPageState.Weight -> WrappedWeightScreen()
                    is WrappedPageState.Summary -> WrappedSummaryScreen()
                    is WrappedPageState.Consistency -> WrappedConsistencyScreen()
                    WrappedPageState.Goals -> WrappedGoalsScreen(interactor = rememberWrappedGoalsInteractor())
                    is WrappedPageState.Progress -> WrappedProgressScreen()
                }
            }
        }
    }
}

internal const val FadeInDelayPerIndex = 100L


@Composable
fun FadeInText(
    delay: Long,
    text: String,
    style: TextStyle = LocalTextStyle.current,
) {
    val alpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(delay)

        alpha.animateTo(1f, tween(300))
    }

    Text(
        modifier = Modifier.alpha(alpha.value),
        text = text,
        textAlign = TextAlign.Center,
        style = style,
    )
}

@Serializable
data class HeavyThing(
    val name: String,
    val weight: Double,
    val icon: String,
)
