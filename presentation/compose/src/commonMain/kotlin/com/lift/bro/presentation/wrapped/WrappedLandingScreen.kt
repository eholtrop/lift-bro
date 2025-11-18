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
import com.lift.bro.data.sqldelight.datasource.toLocalDate
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.fullName
import com.lift.bro.utils.percentageFormat
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun WrappedLandingScreenPreview() {
    WrappedLandingScreen()
}

@Composable
fun WrappedLandingScreen() {
    val sets by dependencies.setRepository.listenAll().collectAsState(emptyList())
    val variations by dependencies.variationRepository.listenAll().collectAsState(emptyList())

    if (sets.isNotEmpty()) {
        HorizontalPager(
            state = rememberPagerState { 5 },
        ) { page ->
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onBackground
            ) {

                when (page) {
                    0 -> {
                        WrappedTenureScreen(earliestSet = sets.minBy { it.date })
                    }

                    1 -> WrappedWeightScreen(sets, variations)
                    2 -> WrappedRepScreen(sets, variations)
                    3 -> WrappedProgressScreen(sets, variations)
                    4 -> WrappedSummaryScreen()
                }
            }

        }
    }
}

private const val FadeInDelayPerIndex = 100L

@Composable
fun WrappedTenureScreen(
    earliestSet: LBSet,
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

                        if (currentYear == earliestSet.date.toLocalDate().year) {
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
                                text = "You've been a lift bro for ${currentYear - earliestSet.date.toLocalDate().year} years!",
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
    sets: List<LBSet>,
    variations: List<Variation>,
) {
    val currentYearSets = sets.filter { it.date.toLocalDate().year == today.year }
    val totalWeight = currentYearSets.sumOf { it.weight * it.reps }
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
                    text = "You moved ${weightFormat(totalWeight)} this year!",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
            }

            item {
                val comparisonObject = heavyThings.toList().random()
                FadeInText(
                    delay = FadeInDelayPerIndex * 2,
                    text = "Thats ${(totalWeight / comparisonObject.weight).decimalFormat()} ${comparisonObject.name}s ${comparisonObject.icon}",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.oneAndHalf))
            }

            item {
                val variationSets = currentYearSets.groupBy { it.variationId }

                val variationWeights = variationSets.map { entry -> variations.first { it.id == entry.key } to entry.value.sumOf { it.weight } }

                val heaviestVariation = variationWeights.maxBy { it.second }

                FadeInText(
                    delay = FadeInDelayPerIndex * 3,
                    text = "You moved ${weightFormat(heaviestVariation.second)} in ${heaviestVariation.first.fullName}s Alone!! \uD83D\uDE35",
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
        icon = "\uD83D\uDC0B"
    ),
)

@Composable
fun WrappedRepScreen(
    sets: List<LBSet>,
    variations: List<Variation>,
) {
    val currentYearSets = sets.filter { it.date.toLocalDate().year == today.year }
    val totalReps = currentYearSets.sumOf { it.reps }

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
                    text = "You picked up $totalReps things this year \uD83D\uDE35",
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
                val dailyAverage = totalReps / if (today.year % 4 == 0) 366 else 365
                val workoutAverage = totalReps / currentYearSets.groupBy { it.date.toLocalDate().dayOfYear }.size

                FadeInText(
                    delay = FadeInDelayPerIndex * 3,
                    text = "Thats an average of ${dailyAverage} reps per day",
                    style = MaterialTheme.typography.bodyLarge
                )


                FadeInText(
                    delay = FadeInDelayPerIndex * 4,
                    text = "or ${workoutAverage} per workout!!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
            }

            item {
                val mostRepsLift = currentYearSets.groupBy { set -> variations.first { it.id == set.variationId } }
                    .map { it.key to it.value.sumOf { it.reps } }
                    .maxBy { it.second }

                FadeInText(
                    delay = FadeInDelayPerIndex * 5,
                    text = "You even did ${mostRepsLift.second} reps of ${mostRepsLift.first.fullName} \uD83D\uDE35",
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
