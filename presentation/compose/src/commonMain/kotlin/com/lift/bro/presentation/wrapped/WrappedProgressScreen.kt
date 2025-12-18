package com.lift.bro.presentation.wrapped

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.data.sqldelight.datasource.toLocalDate
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.fullName
import com.lift.bro.utils.horizontal_padding.padding
import com.lift.bro.utils.listCorners
import com.lift.bro.utils.percentageFormat
import com.lift.bro.utils.toString
import com.lift.bro.utils.vertical_padding.padding
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedProgressScreen(
    items: List<WrappedPageState.ProgressItemState>,
) {
    LiftingScaffold(
        title = {
            Text("You made some GREAT Progress this year!")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
        ) {
            itemsIndexed(items = items) { index, item ->
                ProgressItemView(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large.listCorners(index, items),
                        )
                        .padding(
                            horizontal = MaterialTheme.spacing.half,
                            vertical = MaterialTheme.spacing.half,
                        ),
                    state = item,
                )
            }
        }
    }
}

@Composable
fun ProgressItemView(
    modifier: Modifier = Modifier,
    state: WrappedPageState.ProgressItemState,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Space(MaterialTheme.spacing.half)

        Row(
            modifier = Modifier.fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                )
                .padding(MaterialTheme.spacing.half),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.minWeight?.let { (date, weight, reps) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = date.toString("MMM - d"),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "${weightFormat(weight)} x $reps",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            state.maxWeight?.let { (date, weight, reps) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = date.toString("MMM - d"),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "${weightFormat(weight)} x $reps",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Text(
                modifier = Modifier.weight(1f),
                text = state.progress.percentageFormat(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}


@Preview
@Composable
fun WrappedProgressScreenPreview(@PreviewParameter(DarkModeProvider::class) dark: Boolean) {
    PreviewAppTheme(
        isDarkMode = dark
    ) {
        WrappedProgressScreen(
            items = listOf(
                WrappedPageState.ProgressItemState(
                    title = "Dead Lift",
                    minWeight = WrappedPageState.ProgressItemWeight(
                        date = today,
                        weight = 100.0,
                        reps = 1,
                    ),
                    maxWeight = WrappedPageState.ProgressItemWeight(
                        date = today,
                        weight = 100.0,
                        reps = 1,
                    ),
                    progress = .05,
                ),
                WrappedPageState.ProgressItemState(
                    title = "Back Squat",
                    minWeight = WrappedPageState.ProgressItemWeight(
                        date = today,
                        weight = 100.0,
                        reps = 1,
                    ),
                    maxWeight = WrappedPageState.ProgressItemWeight(
                        date = today,
                        weight = 100.0,
                        reps = 1,
                    ),
                    progress = .1,
                ),
                WrappedPageState.ProgressItemState(
                    title = "Bench Press",
                    minWeight = WrappedPageState.ProgressItemWeight(
                        date = today,
                        weight = 100.0,
                        reps = 1,
                    ),
                    maxWeight = WrappedPageState.ProgressItemWeight(
                        date = today,
                        weight = 100.0,
                        reps = 1,
                    ),
                    progress = .076,
                ),
            )
        )
    }
}
