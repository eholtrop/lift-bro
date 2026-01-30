package com.lift.bro.presentation.wrapped.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.domain.models.VariationId
import com.lift.bro.presentation.wrapped.WrappedPageState.ProgressItemWeight
import com.lift.bro.ui.Space
import com.lift.bro.ui.calendar.today
import com.lift.bro.ui.card.lift.setFormat
import com.lift.bro.ui.theme.icons
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.percentageFormat
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.wrapped_progress_header_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import tv.dpal.compose.padding.vertical.padding
import tv.dpal.compose.toColor
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.ktx.datetime.toLocalDate
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
                            modifier = Modifier.fillMaxWidth()
                                .animateItem(),
                            state = item,
                        )

                        WrappedProgressItemState.Loading ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .animateItem(fadeOutSpec = null)
                                    .padding(
                                        horizontal = MaterialTheme.spacing.half,
                                        vertical = MaterialTheme.spacing.half,
                                    ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier.height(
                                            MaterialTheme.typography.headlineMedium.fontSize.value.dp
                                        )
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
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium,
                    )
            ) {
                if (state.variationId.isNotBlank()) {
                    VariationSetWrappedGraph(
                        modifier = Modifier.height(52.dp).fillMaxWidth().align(Alignment.BottomCenter),
                        id = state.variationId,
                        contentColor = state.variationColor?.toColor() ?: MaterialTheme.colorScheme.primary,
                        highlightDates = listOfNotNull(state.minWeight?.date, state.maxWeight?.date),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
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
}

@Composable
private fun VariationSetWrappedGraph(
    modifier: Modifier = Modifier,
    id: VariationId,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    highlightDates: List<LocalDate>
) {
    var sets by remember { mutableStateOf(emptyList<Pair<LocalDate, Double>>()) }

    LaunchedEffect(Unit) {
        if (id != "") {
            dependencies.setRepository.listenAll(
                startDate = LocalDate(2025, 1, 1),
                endDate = LocalDate(2025, 12, 31),
                variationId = id,
            )
                .map {
                    it.groupBy { it.date.toLocalDate() }.map { (date, sets) -> date to sets.maxOf { it.weight } }.sortedBy { it.first }
                }
                .collect {
                    sets = it
                }
        }
    }

    if (sets.isEmpty()) return

    val minSet = sets.minOf { it.second }
    val maxSet = sets.maxOf { it.second }

    val bubbleColor = MaterialTheme.colorScheme.primary.copy(alpha = .6f)
    Canvas(
        modifier = modifier.fillMaxHeight(),
    ) {
        val width = size.width / sets.size
        val height = size.height
        var x = width / 2f

        sets.forEachIndexed { index, set ->
            val y = height - ((set.second / maxSet).toFloat() * height)
            val previousSet = sets.getOrNull(index - 1)
            x = if (index == 0) x else x + width

            if (highlightDates.contains(set.first)) {
                drawCircle(
                    color = bubbleColor,
                    radius = 5f,
                    center = Offset(x, y)
                )
            }
            previousSet?.let {
                val prevY = height - ((it.second / maxSet).toFloat() * height)
                drawLine(
                    color = bubbleColor,
                    start = Offset(x - width, prevY),
                    end = Offset(x, y)
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
