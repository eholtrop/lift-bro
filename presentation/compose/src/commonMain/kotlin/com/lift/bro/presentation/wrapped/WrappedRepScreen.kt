package com.lift.bro.presentation.wrapped

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.domain.models.fullName
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.wrapped.usecase.GetTotalRepsUseCase
import com.lift.bro.presentation.wrapped.usecase.GetVariationWithMostRepsUseCase
import com.lift.bro.presentation.wrapped.usecase.GetWorkoutAverageUseCase
import com.lift.bro.ui.dialog.InfoSpeechBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.format
import com.lift.bro.utils.vertical_padding.padding
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.wrapped_reps_daily_average
import lift_bro.core.generated.resources.wrapped_reps_header_title
import lift_bro.core.generated.resources.wrapped_reps_most_reps_lift
import lift_bro.core.generated.resources.wrapped_reps_speech_bubble_title
import lift_bro.core.generated.resources.wrapped_reps_total_subtitle
import lift_bro.core.generated.resources.wrapped_reps_total_title
import lift_bro.core.generated.resources.wrapped_reps_workout_average
import org.jetbrains.compose.resources.stringResource

@Serializable
data class WrappedRepState(
    val totalReps: Long,
    val dailyAverage: Long,
    val workoutAverage: Long,
    val mostRepsLift: Pair<String, Long>,
)

@Composable
fun rememberWrappedRepsInteractor(
    year: Int = LocalWrappedYear.current,
    getTotalRepsUseCase: GetTotalRepsUseCase = GetTotalRepsUseCase(),
    getVariationWithMostRepsUseCase: GetVariationWithMostRepsUseCase = GetVariationWithMostRepsUseCase(),
    getWorkoutAverageUseCase: GetWorkoutAverageUseCase = GetWorkoutAverageUseCase(),
) = rememberInteractor<WrappedRepState?, Nothing>(
    initialState = null,
    source = {
        combine(
            getTotalRepsUseCase(
                startDate = LocalDate(year, 1, 1),
                endDate = LocalDate(year, 12, 31)

            ),
            getVariationWithMostRepsUseCase(
                startDate = LocalDate(year, 1, 1),
                endDate = LocalDate(year, 12, 31)

            ),
            getWorkoutAverageUseCase(
                startDate = LocalDate(year, 1, 1),
                endDate = LocalDate(year, 12, 31)
            )
        ) { totalReps, mostVariationReps, workoutAverage ->
            WrappedRepState(
                totalReps = totalReps,
                dailyAverage = totalReps / if (year % 4 == 0) 366 else 365,
                workoutAverage = workoutAverage,
                mostRepsLift = (mostVariationReps?.first?.fullName ?: "") to (mostVariationReps?.second ?: 0L)
            )
        }
    }
)

@Composable
fun WrappedRepScreen(
    interactor: Interactor<WrappedRepState?, Nothing> = rememberWrappedRepsInteractor(),
) {
    val state by interactor.state.collectAsState()

    state?.let {
        WrappedRepScreen(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedRepScreen(
    state: WrappedRepState,
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
                text = stringResource(Res.string.wrapped_reps_header_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            FadeInText(
                delay = FadeInDelayPerIndex * 1,
                text = stringResource(Res.string.wrapped_reps_total_title, state.totalReps.format()),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.half))
        }

        item {
            FadeInText(
                delay = FadeInDelayPerIndex * 2,
                text = stringResource(Res.string.wrapped_reps_total_subtitle),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
        }

        item {
            InfoSpeechBubble(
                title = {
                    Text(
                        stringResource(Res.string.wrapped_reps_speech_bubble_title),
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                message = {
                    FadeInText(
                        delay = FadeInDelayPerIndex * 3,
                        text = stringResource(Res.string.wrapped_reps_daily_average, state.dailyAverage),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FadeInText(
                        delay = FadeInDelayPerIndex * 4,
                        text = stringResource(Res.string.wrapped_reps_workout_average, state.workoutAverage),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.half))
                    FadeInText(
                        delay = FadeInDelayPerIndex * 5,
                        text = stringResource(
                            Res.string.wrapped_reps_most_reps_lift,
                            state.mostRepsLift.second.format(),
                            state.mostRepsLift.first
                        ),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        }
    }
}
