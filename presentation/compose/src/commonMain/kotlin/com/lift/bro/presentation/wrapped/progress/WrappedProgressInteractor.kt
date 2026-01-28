package com.lift.bro.presentation.wrapped.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.lift.bro.domain.models.VariationId
import tv.dpal.flowvi.rememberInteractor
import com.lift.bro.presentation.wrapped.LocalWrappedYear
import com.lift.bro.presentation.wrapped.WrappedPageState.ProgressItemWeight
import com.lift.bro.presentation.wrapped.usecase.GetVariationProgressUseCase
import com.lift.bro.utils.fullName
import com.lift.bro.`ktx-datetime`.toLocalDate
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

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
        val variationId: VariationId = "",
    ) : WrappedProgressItemState()

    @Serializable
    data object Loading : WrappedProgressItemState()
}

@Composable
fun rememberWrappedProgressInteractor(
    year: Int = LocalWrappedYear.current,
    getVariationProgressUseCase: GetVariationProgressUseCase = GetVariationProgressUseCase(),
) = rememberInteractor<WrappedProgressState, Nothing>(
    initialState = WrappedProgressState(
        items = listOf(
            WrappedProgressItemState.Loading,
            WrappedProgressItemState.Loading,
            WrappedProgressItemState.Loading
        )
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
                            isBodyWeight = variation.bodyWeight ?: false,
                            variationId = variation.id
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
