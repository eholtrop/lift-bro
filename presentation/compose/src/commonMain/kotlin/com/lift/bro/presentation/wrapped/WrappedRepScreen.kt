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
                text = "Total Reps",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            FadeInText(
                delay = FadeInDelayPerIndex * 1,
                text = "You picked up ${state.totalReps} things this year \uD83D\uDE35",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.half))
        }

        item {
            FadeInText(
                delay = FadeInDelayPerIndex * 2,
                text = "(And then you put them down again)",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
        }

        item {
            InfoSpeachBubble(
                title = {
                    Text("WOW")
                },
                message = {
                    FadeInText(
                        delay = FadeInDelayPerIndex * 5,
                        text = "You even did ${state.mostRepsLift.second} reps of ${state.mostRepsLift.first} \uD83D\uDE35",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    FadeInText(
                        delay = FadeInDelayPerIndex * 3,
                        text = "Thats an average of ${state.dailyAverage} reps per day",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
                    FadeInText(
                        delay = FadeInDelayPerIndex * 4,
                        text = "or ${state.workoutAverage} per workout!!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}
