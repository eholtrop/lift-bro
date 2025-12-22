package com.lift.bro.presentation.wrapped

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.dialog.InfoSpeachBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.vertical_padding.padding
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedRepScreen(
    state: WrappedPageState.Reps,
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
                text = stringResource(Res.string.wrapped_reps_total_title, state.totalReps),
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
            InfoSpeachBubble(
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
                        text = stringResource(Res.string.wrapped_reps_most_reps_lift, state.mostRepsLift.second, state.mostRepsLift.first),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            )
        }
    }
}
