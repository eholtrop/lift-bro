package com.lift.bro.presentation.wrapped

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.data.core.repository.SetRepository
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.dialog.InfoSpeachBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.combine
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.fullName
import com.lift.bro.utils.vertical_padding.padding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class WrappedWeightState(
    val totalWeightMoved: Double,
    val heavyThings: List<Pair<HeavyThing, Double>>,
    val heaviestVariation: Pair<String, Double>,
)

class GetTotalWeightMovedUseCase(
    private val setRepository: ISetRepository = dependencies.setRepository,
) {

    /*
     * Fetches the total weight moved for between the given dates
     */
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Double> = setRepository.listenAll(
        startDate = startDate,
        endDate = endDate
    ).map { sets -> sets.sumOf { it.weight * it.reps } }
}

/*
 * Gets the variation with the most weight moved between the given dates
 */
class GetVariationWithMostWeightMovedUseCase(
    private val setRepository: ISetRepository = dependencies.setRepository,
    private val variationRepository: IVariationRepository = dependencies.variationRepository,
) {

    /*
     * Fetches the total weight moved for between the given dates
     */
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Pair<Variation, Double>> = combine(
        setRepository.listenAll(
            startDate = startDate,
            endDate = endDate
        ),
        variationRepository.listenAll(),
    ) { sets, variations ->
        sets.groupBy { set -> variations.first { it.id == set.variationId } }
            .map { entry -> entry.key to entry.value.sumOf { it.weight } }
            .maxBy { it.second }
    }
}

@Composable
fun rememberWrappedWeightInteractor(
    getTotalWeightMovedUseCase: GetTotalWeightMovedUseCase = GetTotalWeightMovedUseCase(),
    getVariationWithMostWeightMovedUseCase: GetVariationWithMostWeightMovedUseCase = GetVariationWithMostWeightMovedUseCase(),
) = rememberInteractor<WrappedWeightState?, Nothing>(
    initialState = null,
    source = {
        combine(
            getTotalWeightMovedUseCase(
                startDate = LocalDate(2025, 1, 1),
                endDate = LocalDate(2025, 12, 31)
            ),
            getVariationWithMostWeightMovedUseCase(
                startDate = LocalDate(2025, 1, 1),
                endDate = LocalDate(2025, 12, 31)
            )
        ) { twm, varTwm ->
            WrappedWeightState(
                totalWeightMoved = twm,
                heavyThings = heavyThings.map { it to twm / it.weight },
                heaviestVariation = varTwm.first.fullName to varTwm.second,
            )
        }
    }
)

@Composable
fun WrappedWeightScreen(
    interactor: Interactor<WrappedWeightState?, Nothing> = rememberWrappedWeightInteractor(),
) {
    val state by interactor.state.collectAsState()

    state?.let {
        WrappedWeightScreen(state = it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedWeightScreen(
    state: WrappedWeightState,
) {
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
                text = "Total Weight Moved",
                style = MaterialTheme.typography.headlineMedium
            )
        }
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
                text = "Thats...",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        itemsIndexed(state.heavyThings) { index, (thing, reps) ->
            FadeInText(
                delay = FadeInDelayPerIndex * 3 + index,
                text = "${reps.decimalFormat()} ${thing.name}s ${thing.icon}",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
        }

        item {
            InfoSpeachBubble(
                title = {
                    FadeInText(
                        delay = FadeInDelayPerIndex * (4 + heavyThings.size),
                        text = "Thats HUGE!!!! \uD83D\uDCAA",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                message = {
                    FadeInText(
                        delay = FadeInDelayPerIndex * (4 + heavyThings.size),
                        text = "You moved ${weightFormat(state.heaviestVariation.second)} in ${state.heaviestVariation.first}s Alone!! \uD83D\uDE35",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            )
        }
    }
}
