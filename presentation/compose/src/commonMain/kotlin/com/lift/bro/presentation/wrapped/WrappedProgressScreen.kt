package com.lift.bro.presentation.wrapped

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.wrapped.WrappedPageState.ProgressItemWeight
import com.lift.bro.presentation.wrapped.usecase.GetVariationProgressUseCase
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.setFormat
import com.lift.bro.ui.theme.Icons
import com.lift.bro.ui.theme.icons
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.fullName
import com.lift.bro.utils.horizontal_padding.padding
import com.lift.bro.utils.listCorners
import com.lift.bro.utils.percentageFormat
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import com.lift.bro.utils.vertical_padding.padding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.delayEach
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.wrapped_progress_header_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import kotlin.random.Random

@Serializable
@Immutable
data class WrappedProgressState(
    val items: List<WrappedProgressItemState>,
)


@Serializable
sealed class WrappedProgressItemState {

    @Serializable
    data class Loaded(
        val title: String,
        val minWeight: ProgressItemWeight?,
        val maxWeight: ProgressItemWeight?,
        val progress: Double,
        val favourite: Boolean,
        val variationColor: ULong? = null,
        val isBodyWeight: Boolean = false,
    ): WrappedProgressItemState()

    @Serializable
    data object Loading: WrappedProgressItemState()
}

@Composable
fun rememberWrappedProgressInteractor(
    year: Int = 2025,
    getVariationProgressUseCase: GetVariationProgressUseCase = GetVariationProgressUseCase(),
) = rememberInteractor<WrappedProgressState, Nothing>(
    initialState = WrappedProgressState(
        items = listOf(WrappedProgressItemState.Loading, WrappedProgressItemState.Loading, WrappedProgressItemState.Loading)
    ),
    source = {
        getVariationProgressUseCase(
            startDate = LocalDate(year, 1, 1),
            endDate = LocalDate(year, 12, 31),
        )
            .map { it.filterValues { it != null }.mapValues { it.value!! } }
            .map { variations ->
                WrappedProgressState(
                    variations.map { (variation, progress) ->
                        WrappedProgressItemState.Loaded(
                            title = variation.fullName,
                            maxWeight = ProgressItemWeight(
                                date = progress.maxSet.date.toLocalDate(),
                                weight = progress.maxSet.weight,
                                reps = progress.maxSet.reps,
                            ),
                            minWeight = ProgressItemWeight(
                                date = progress.minSet.date.toLocalDate(),
                                weight = progress.minSet.weight,
                                reps = progress.minSet.reps,
                            ),
                            progress = when (variation.bodyWeight) {
                                true -> (progress.maxSet.reps - progress.minSet.reps) / progress.minSet.reps
                                else -> (progress.maxSet.weight - progress.minSet.weight) / progress.minSet.weight
                            }.toDouble().let {
                                if (it.isNaN()) 0.0 else it
                            },
                            favourite = variation.favourite,
                            variationColor = variation.lift?.color,
                            isBodyWeight = variation.bodyWeight ?: false
                        )
                    }
                        .sortedWith(
                            compareByDescending<WrappedProgressItemState> { (it as? WrappedProgressItemState.Loaded)?.favourite }
                                .thenByDescending { (it as? WrappedProgressItemState.Loaded)?.progress ?: 0.0 }
                        )
                )
            }

    }
)

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
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
                text = stringResource(Res.string.wrapped_progress_header_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
        }

        itemsIndexed(
            items = items,
        ) { index, item ->
            when (item) {
                is WrappedProgressItemState.Loaded -> ProgressItemView(
                    modifier = Modifier.fillMaxWidth()
                        .animateItem(
                            fadeOutSpec = null
                        )
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

                WrappedProgressItemState.Loading ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .animateItem()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.large.listCorners(index, items),
                            )
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
                style = MaterialTheme.typography.headlineMedium
            )
            if (state.favourite) {
                Space(MaterialTheme.spacing.one)
                Icon(
                    imageVector = MaterialTheme.icons.favourite,
                    contentDescription = "Favourite",
                    tint = state.variationColor?.toColor() ?: MaterialTheme.colorScheme.primary
                )
            }
        }

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
                        favourite = true,
                    ),
                    WrappedProgressItemState.Loaded(
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
                        favourite = false,
                    ),
                    WrappedProgressItemState.Loaded(
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
                        favourite = true,
                    ),
                )
            )
        )
    }
}
