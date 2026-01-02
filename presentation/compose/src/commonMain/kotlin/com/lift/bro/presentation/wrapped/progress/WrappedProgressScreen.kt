package com.lift.bro.presentation.wrapped.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.wrapped.WrappedPageState.ProgressItemWeight
import com.lift.bro.ui.Space
import com.lift.bro.ui.setFormat
import com.lift.bro.ui.theme.icons
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.percentageFormat
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toString
import com.lift.bro.utils.vertical_padding.padding
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.wrapped_progress_header_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import kotlin.random.Random


@Composable
fun WrappedProgressScreen(
    interactor: Interactor<WrappedProgressState, Nothing> = rememberWrappedProgressInteractor(),
) {
    val state by interactor.state.collectAsState()

    WrappedProgressScreen(state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedProgressScreen(
    state: WrappedProgressState,
) {
    val items = state.items

    Column {
        Text(
            modifier = Modifier
                .background(color = BottomSheetDefaults.ContainerColor)
                .padding(
                    horizontal = MaterialTheme.spacing.one,
                    top = MaterialTheme.spacing.oneAndHalf,
                    bottom = MaterialTheme.spacing.threeQuarters
                ),
            text = stringResource(Res.string.wrapped_progress_header_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Box(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.half)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0f),
                            ),
                        ),
                        shape = MaterialTheme.shapes.large
                    )
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
            ) {
                itemsIndexed(
                    items = items,
                ) { index, item ->
                    when (item) {
                        is WrappedProgressItemState.Loaded -> ProgressItemView(
                            modifier = Modifier.fillMaxWidth(),
                            state = item,
                        )

                        WrappedProgressItemState.Loading ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(
                                        horizontal = MaterialTheme.spacing.half,
                                        vertical = MaterialTheme.spacing.half,
                                    ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier.height(MaterialTheme.typography.headlineMedium.fontSize.value.dp)
                                            .fillParentMaxWidth(Random.nextDouble(.33, .66).toFloat())
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainer,
                                                shape = MaterialTheme.shapes.medium,
                                            ),
                                    )
                                }

                                Space(MaterialTheme.spacing.half)

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .height(72.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceContainer,
                                            shape = MaterialTheme.shapes.medium,
                                        )
                                        .padding(MaterialTheme.spacing.half),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                }
                            }
                    }

                }
            }
        }
    }
}

@Composable
fun ProgressItemView(
    modifier: Modifier = Modifier,
    state: WrappedProgressItemState.Loaded,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onTertiary
            )
            if (state.favourite) {
                Space(MaterialTheme.spacing.one)
                Box {
                    Icon(
                        imageVector = MaterialTheme.icons.favourite,
                        contentDescription = "Favourite",
                        tint = state.variationColor?.toColor() ?: MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = MaterialTheme.icons.favouriteOutlined,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }

        Space(MaterialTheme.spacing.half)

        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
        ) {
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
                            text = date.toString("MMM d"),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = setFormat(weight, reps.toInt(), bodyWeight = state.isBodyWeight),
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
                            text = date.toString("MMM d"),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = setFormat(weight, reps.toInt(), bodyWeight = state.isBodyWeight),
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
}


@Preview
@Composable
fun WrappedProgressScreenPreview(@PreviewParameter(DarkModeProvider::class) dark: Boolean) {
    PreviewAppTheme(
        isDarkMode = dark
    ) {
        WrappedProgressScreen(
            state = WrappedProgressState(
                items = listOf(
                    WrappedProgressItemState.Loaded(
                        title = "Dead Lift",
                        minWeight = ProgressItemWeight(
                            date = today,
                            weight = 100.0,
                            reps = 1,
                        ),
                        maxWeight = ProgressItemWeight(
                            date = today,
                            weight = 100.0,
                            reps = 1,
                        ),
                        progress = .05,
                        favourite = true,
                    ),
                    WrappedProgressItemState.Loaded(
                        title = "Back Squat",
                        minWeight = ProgressItemWeight(
                            date = today,
                            weight = 100.0,
                            reps = 1,
                        ),
                        maxWeight = ProgressItemWeight(
                            date = today,
                            weight = 100.0,
                            reps = 1,
                        ),
                        progress = .1,
                        favourite = false,
                    ),
                    WrappedProgressItemState.Loaded(
                        title = "Bench Press",
                        minWeight = ProgressItemWeight(
                            date = today,
                            weight = 100.0,
                            reps = 1,
                        ),
                        maxWeight = ProgressItemWeight(
                            date = today,
                            weight = 100.0,
                            reps = 1,
                        ),
                        progress = .076,
                        favourite = true,
                    ),
                )
            )
        )
    }
}
