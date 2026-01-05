package com.lift.bro.presentation.wrapped.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.domain.models.Goal
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.wrapped.heavyThings
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.format
import com.lift.bro.utils.percentageFormat
import com.lift.bro.utils.vertical_padding.padding
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WrappedSummaryScreen(
    interactor: Interactor<WrappedSummaryState?, Nothing> = rememberWrappedSummaryInteractor(),
) {
    val state by interactor.state.collectAsState()

    state?.let {
        WrappedSummaryScreen(
            state = it
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedSummaryScreen(
    state: WrappedSummaryState,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
    ) {
        stickyHeader {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = BottomSheetDefaults.ContainerColor)
                    .padding(
                        horizontal = MaterialTheme.spacing.one,
                        top = MaterialTheme.spacing.oneAndHalf,
                        bottom = MaterialTheme.spacing.threeQuarters
                    ),
                text = "What a great year!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        item {
            with(state.weight) {
                WrappedSummaryCard(
                    title = "Total Weight Moved",
                    cards = listOf(
                        {
                            Text(
                                text = weightFormat(totalWeightMoved, useGrouping = true),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        },
                        {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = weightFormat(heaviestVariationWeight, useGrouping = true),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = heaviestVariationName,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                        {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = numOfHeavyThings.decimalFormat(showDecimal = true),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = "${heavyThing.name}s ${heavyThing.icon}",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                    )
                )
            }
        }

        item {
            with(state.reps) {
                WrappedSummaryCard(
                    title = "Reps",
                    cards = listOf(
                        {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = totalReps.format(),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = "Total Reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                        {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = variationReps.format(),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = variationName,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                        {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = repsPerDay.format(),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = "per day!",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        },
                    )
                )
            }
        }

        item {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(all = MaterialTheme.spacing.half),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        state.progression.forEach {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = it.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                                    ) {
                                        Text(
                                            text = weightFormat(it.minWeight),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                        Icon(
                                            modifier = Modifier.size(MaterialTheme.spacing.threeQuarters),
                                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                                            contentDescription = "To"
                                        )
                                        Text(
                                            text = weightFormat(it.maxWeight),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                Space(MaterialTheme.spacing.half)
                                Text(text = it.progress.percentageFormat())
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(all = MaterialTheme.spacing.half),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Consistency",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        state.consistencies.forEach {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Space(MaterialTheme.spacing.half)
                                Text(
                                    text = "${it.occurrences}x"
                                )
                            }
                        }
                    }
                }
            }
        }

        stickyHeader {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = BottomSheetDefaults.ContainerColor)
                    .padding(
                        horizontal = MaterialTheme.spacing.one,
                        top = MaterialTheme.spacing.oneAndHalf,
                        bottom = MaterialTheme.spacing.threeQuarters
                    ),
                text = "Onto the next one!!",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (state.goals.isNotEmpty()) {
            item {
                Text("Goals")
            }

            items(items = state.goals) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        it.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun WrappedSummaryCard(
    title: String,
    cards: List<@Composable () -> Unit>,
) {
    Column(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
        )
            .padding(
                top = MaterialTheme.spacing.one,
                bottom = MaterialTheme.spacing.half
            )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Space(MaterialTheme.spacing.half)
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.half),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
        ) {
            cards.forEach {
                Box(
                    modifier = Modifier.weight(1f).aspectRatio(1f)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.medium,
                        ).padding(MaterialTheme.spacing.half),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium
                    ) {
                        it()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun WrappedSummaryScreenPreview(@PreviewParameter(DarkModeProvider::class) dark: Boolean) {
    PreviewAppTheme(isDarkMode = dark) {
        WrappedSummaryScreen(
            state = WrappedSummaryState(
                weight = WrappedSummaryWeightState(
                    totalWeightMoved = 269336.5,
                    heaviestVariationWeight = 9935.0,
                    heaviestVariationName = "Regular Deadlift",
                    numOfHeavyThings = 17.453,
                    heavyThing = heavyThings.random()

                ),
                reps = WrappedSummaryRepsState(
                    totalReps = 3302,
                    variationReps = 382,
                    variationName = "Leg Press",
                    repsPerDay = 9
                ),
                consistencies = listOf(
                    WrappedSummaryConsistencyState(
                        title = "May",
                        occurrences = 10
                    ),
                    WrappedSummaryConsistencyState(
                        title = "Monday",
                        occurrences = 18
                    ),
                    WrappedSummaryConsistencyState(
                        title = "Dead Lift",
                        occurrences = 20
                    )
                ),
                progression = listOf(
                    WrappedSummaryProgressState(
                        title = "Leg Press",
                        progress = .8,
                        minWeight = 12.5,
                        maxWeight = 14.2,
                    ),
                    WrappedSummaryProgressState(
                        title = "Dead Lift",
                        progress = .8,
                        minWeight = 12.5,
                        maxWeight = 14.2,
                    ),
                    WrappedSummaryProgressState(
                        title = "Bench Press",
                        progress = .8,
                        minWeight = 12.5,
                        maxWeight = 14.2,
                    )
                ),
                goals = listOf(
                    Goal(
                        name = "Leg Press"
                    ),
                    Goal(
                        name = "Dead Lift"
                    ),
                    Goal(
                        name = "Bench Press"
                    )
                )
            )
        )
    }
}
