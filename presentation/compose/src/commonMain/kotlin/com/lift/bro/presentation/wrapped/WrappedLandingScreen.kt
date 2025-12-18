@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.wrapped

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.compose.AppTheme
import com.lift.bro.data.sqldelight.datasource.toLocalDate
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.fullName
import com.lift.bro.utils.percentageFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun WrappedLandingScreenPreview() {
    PreviewAppTheme(isDarkMode = true) {
        WrappedLandingScreen(
            state = WrappedState(
                listOf(
                    WrappedPageState.Tenure(1)
                )
            )
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
    data class Tenure(
        val year: Int,
    ): WrappedPageState()

    @Serializable
    data class Weight(
        val totalWeightMoved: Double,
        val heavyThing: HeavyThing,
        val heaviestVariation: Pair<String, Double>,
    ): WrappedPageState()

    @Serializable
    data class Reps(
        val totalReps: Long,
        val dailyAverage: Long,
        val workoutAverage: Long,
        val mostRepsLift: Pair<String, Long>
    ): WrappedPageState()

    @Serializable
    data class Summary(
        val sets: List<LBSet>,
    ): WrappedPageState()
}

sealed class WrappedEvents()

@Composable
fun rememberWrappedInteractor(
    setRepository: ISetRepository = dependencies.setRepository,
    variationRepository: IVariationRepository = dependencies.variationRepository,
): Interactor<WrappedState, WrappedEvents> {
    return rememberInteractor(
        initialState = WrappedState(),
        source = {
            combine(
                setRepository.listenAll(),
                variationRepository.listenAll(),
            ) { sets, variations ->
                val variationSets = sets.groupBy { it.variationId }


                WrappedState(
                    pages = listOf(
                        WrappedPageState.Tenure(year = sets.minOf { it.date.toLocalDate().year }),
                        WrappedPageState.Weight(
                            totalWeightMoved = sets.sumOf { it.weight * it.reps },
                            heavyThing = heavyThings.toList().random(),
                            heaviestVariation = variationSets.map { entry -> variations.first { it.id == entry.key }.fullName to entry.value.sumOf { it.weight } }
                                .maxBy { it.second }
                        ),
                        WrappedPageState.Reps(
                            totalReps = sets.sumOf { it.reps },
                            dailyAverage = sets.sumOf { it.reps / if (today.year % 4 == 0) 366 else 365 },
                            workoutAverage = sets.sumOf { it.reps / sets.groupBy { it.date.toLocalDate().dayOfYear }.size },
                            mostRepsLift = variations.first { it.id == sets.maxBy { it.reps }.variationId }.fullName to sets.maxOf { it.reps }
                        ),
                        WrappedPageState.Summary(
                            sets = sets
                        ),
                    )
                )
            }
        }
    )
}

@Composable
fun WrappedLandingScreen(
    interactor: Interactor<WrappedState, WrappedEvents> = rememberWrappedInteractor(),
) {
    val state by interactor.state.collectAsState()

}

@Composable
fun WrappedLandingScreen(
    state: WrappedState,
) {
    HorizontalPager(
        state = rememberPagerState { state.pages.size },
    ) { page ->
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground
        ) {

            when (val page = state.pages.get(page)) {
                is WrappedPageState.Reps -> WrappedRepScreen(page)
                is WrappedPageState.Tenure -> WrappedTenureScreen(page)
                is WrappedPageState.Weight -> WrappedWeightScreen(page)
                is WrappedPageState.Summary -> WrappedSummaryScreen()

            }
        }

    }
}

private const val FadeInDelayPerIndex = 100L

@Composable
fun WrappedTenureScreen(
    state: WrappedPageState.Tenure,
) {
    val currentYear = today.year

    LiftingScaffold(
        title = { Text("Welcome to Lift Bro Wrapped!") },
        content = { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        if (currentYear == state.year) {
                            FadeInText(
                                delay = 100L,
                                text = "This was your first Lift Bro year! \uD83C\uDF89",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Space(MaterialTheme.spacing.one)
                            FadeInText(
                                delay = 200L,
                                text = "Whether you started using lift bro this year, or started on your lifting journey",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Space(MaterialTheme.spacing.half)
                            FadeInText(
                                delay = 300L,
                                text = "Thank YOU!!!",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Space(MaterialTheme.spacing.one)
                            FadeInText(
                                delay = 400L,
                                text = "Here's to the next year! \uD83E\uDD73 \uD83E\uDD42",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else {
                            FadeInText(
                                delay = 500L,
                                text = "You've been a lift bro for ${currentYear - state.year} years!",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Space(MaterialTheme.spacing.one)
                            FadeInText(
                                delay = 600L,
                                text = "Congrats!! and thank YOU for the support",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
                }

                item {
                    if (LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro) {
                        FadeInText(
                            delay = 800L,
                            text = "And a VERY EXTRA SPECIAL THANK YOU! for being a Lift PRO!!!",
                            style = MaterialTheme.typography.titleLarge
                        )
                        FadeInText(
                            delay = 1000L,
                            text = "Support like yours is what keeps this app going!",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun WrappedWeightScreen(
    state: WrappedPageState.Weight,
) {
    LiftingScaffold(
        title = {
            Text("Total Weight Moved")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
        ) {
            item {
                FadeInText(
                    delay = FadeInDelayPerIndex * 1,
                    text = "You moved ${weightFormat(state.totalWeightMoved)} this year!",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
            }

            item {
                FadeInText(
                    delay = FadeInDelayPerIndex * 2,
                    text = "Thats ${(state.totalWeightMoved / state.heavyThing.weight).decimalFormat()} ${state.heavyThing.name}s ${state.heavyThing.icon}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.oneAndHalf))
            }

            item {

                FadeInText(
                    delay = FadeInDelayPerIndex * 3,
                    text = "You moved ${weightFormat(state.heaviestVariation.second)} in ${state.heaviestVariation.first}s Alone!! \uD83D\uDE35",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.half))
            }
            item {
                FadeInText(
                    delay = FadeInDelayPerIndex * 4,
                    text = "Thats HUGE!!!! \uD83D\uDCAA",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
fun FadeInText(
    delay: Long,
    text: String,
    style: TextStyle,
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

private val heavyThings = listOf(
    HeavyThing(
        name = "Blue Whale",
        weight = 300000.0,
        icon = "\uD83D\uDC0B"
    ),
    HeavyThing(
        name = "Elephant",
        weight = 15432.0,
        icon = "\uD83D\uDC18"
    ),
)

@Composable
fun WrappedRepScreen(
    state: WrappedPageState.Reps,
) {
    LiftingScaffold(
        title = {
            Text("Total Reps")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
        ) {
            item {
                FadeInText(
                    delay = FadeInDelayPerIndex * 1,
                    text = "You picked up ${state.totalReps} things this year \uD83D\uDE35",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.half))
            }

            item {
                FadeInText(
                    delay = FadeInDelayPerIndex * 2,
                    text = "(And then you put them down again)",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.quarter))
            }

            item {
                FadeInText(
                    delay = FadeInDelayPerIndex * 3,
                    text = "Thats an average of ${state.dailyAverage} reps per day",
                    style = MaterialTheme.typography.bodyLarge
                )


                FadeInText(
                    delay = FadeInDelayPerIndex * 4,
                    text = "or ${state.workoutAverage} per workout!!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
            }

            item {
                FadeInText(
                    delay = FadeInDelayPerIndex * 5,
                    text = "You even did ${state.mostRepsLift.second} reps of ${state.mostRepsLift.first} \uD83D\uDE35",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

@Composable
fun WrappedProgressScreen(
    sets: List<LBSet>,
    variations: List<Variation>,
) {
    val currentYearSets = sets.filter { it.date.toLocalDate().year == today.year }

    val minMaxVariations = currentYearSets.groupBy { set -> variations.first { it.id == set.variationId } }
        .map { it.key to (it.value.minBy { it.weight } to (it.value.maxBy { it.weight })) }
        .toList()
        .sortedBy { it.second.second.weight }

    LiftingScaffold(
        title = {
            Text("You made some GREAT Progress this year!")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(MaterialTheme.spacing.one)
        ) {

            item {
                Row {
                    Text(
                        modifier = Modifier.weight(.5f),
                        text = "Lift Name"
                    )

                    Text(
                        modifier = Modifier.weight(.2f),
                        text = "${today.year - 1}"
                    )

                    Text(
                        modifier = Modifier.weight(.2f),
                        text = "${today.year}"
                    )
                    Text(
                        modifier = Modifier.weight(.1f),
                        text = "% GAINS"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
            }

            items(items = minMaxVariations) {
                Row {
                    Text(
                        modifier = Modifier.weight(.5f),
                        text = it.first.fullName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Space(MaterialTheme.spacing.one)

                    val bodyWeight = it.first.bodyWeight == true

                    Text(
                        modifier = Modifier.weight(.2f),
                        text = if (bodyWeight) "${it.second.first.reps} reps" else weightFormat(it.second.first.weight),

                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Space(MaterialTheme.spacing.half)
                    Text(
                        modifier = Modifier.weight(.2f),
                        text = if (bodyWeight) "${it.second.second.reps} reps" else weightFormat(it.second.second.weight),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Space(MaterialTheme.spacing.half)
                    val progress = if (bodyWeight) {
                        ((it.second.second.reps - it.second.first.reps) / it.second.first.reps).toDouble()
                    } else {
                        ((it.second.second.weight - it.second.first.weight) / it.second.first.weight)
                    }
                    Text(
                        modifier = Modifier.weight(.1f),
                        text = progress.percentageFormat(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun WrappedSummaryScreen() {

}
