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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.percentageFormat
import com.lift.bro.utils.vertical_padding.padding
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedSummaryScreen() {
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
            WrappedSummaryCard(
                title = "Total Weight Moved",
                cards = listOf(
                    { Text("234234.6 lbs") },
                    { Text("2342.5 lbs in Dead Lifts") },
                    { Text("Thats 17 elephants!") },
                )
            )
        }

        item {
            WrappedSummaryCard(
                title = "Reps",
                cards = listOf(
                    { Text("2345 Total Reps!") },
                    { Text("336 in Leg Press Alone!!") },
                    { Text("44 per day this year!") },
                )
            )
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

                        listOf(
                            WrappedSummaryConsistencyState(
                                title = "July",
                                occurrences = 10,
                            ),
                            WrappedSummaryConsistencyState(
                                title = "Monday",
                                occurrences = 15,
                            ),
                            WrappedSummaryConsistencyState(
                                title = "Dead Lift",
                                occurrences = 30,
                            ),
                        ).forEach {
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
                        listOf(
                            WrappedSummaryProgressState(
                                title = "Dead Lift",
                                progress = .6,
                                minWeight = 100.0,
                                maxWeight = 200.0,
                            ),
                            WrappedSummaryProgressState(
                                title = "Back Squat",
                                progress = .7,
                                minWeight = 100.0,
                                maxWeight = 200.0,
                            ),
                            WrappedSummaryProgressState(
                                title = "Leg Press",
                                progress = .7,
                                minWeight = 100.0,
                                maxWeight = 200.0,
                            )
                        ).forEach {
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
