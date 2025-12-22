package com.lift.bro.presentation.wrapped.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.Interactor
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.decimalFormat
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
                    .background(color = BottomSheetDefaults.ContainerColor)
                    .padding(
                        horizontal = MaterialTheme.spacing.one,
                        top = MaterialTheme.spacing.oneAndHalf,
                        bottom = MaterialTheme.spacing.threeQuarters
                    ),
                text = "What a great year!",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            with(state.weight) {
                WrappedSummaryCard(
                    title = "Total Weight Moved",
                    cards = listOf(
                        {
                            Text(
                                text = weightFormat(totalWeightMoved),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        },
                        {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = weightFormat(heaviestVariationWeight),
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
                                    text = numOfHeavyThings.decimalFormat(),
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
                                    text = totalReps.toString(),
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
                                    text = variationReps.toString(),
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
                                    text = repsPerDay.toString(),
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
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .height(IntrinsicSize.Max)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Consistency",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        state.consistencies.forEach {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = MaterialTheme.spacing.half,
                                )
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = it.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = it.occurrences.toString()
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        state.progression.forEach {
                            Column(
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.half),
                            ) {
                                Row {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = it.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(text = it.progress.percentageFormat())
                                }
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
                        }
                    }
                }
            }
        }


        stickyHeader {
            Text(
                modifier = Modifier
                    .background(color = BottomSheetDefaults.ContainerColor)
                    .padding(
                        horizontal = MaterialTheme.spacing.one,
                        top = MaterialTheme.spacing.oneAndHalf,
                        bottom = MaterialTheme.spacing.threeQuarters
                    ),
                text = "Onto the next year!!",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text("Goals")
        }

        items(items = listOf("awef", "awef", "tghertihj")) {
            Box(
                Modifier
            ) {

            }
            Text(it)
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
                horizontal = MaterialTheme.spacing.one,
                top = MaterialTheme.spacing.one,
                bottom = MaterialTheme.spacing.half
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Space(MaterialTheme.spacing.half)
        Row(
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
        WrappedSummaryScreen()
    }
}
