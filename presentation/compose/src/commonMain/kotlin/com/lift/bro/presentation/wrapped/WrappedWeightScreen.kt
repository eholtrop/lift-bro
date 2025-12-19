package com.lift.bro.presentation.wrapped

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.vertical_padding.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedWeightScreen(
    state: WrappedPageState.Weight,
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
                text = "Thats ${(state.totalWeightMoved / state.heavyThing.weight).decimalFormat()} ${state.heavyThing.name}s ${state.heavyThing.icon}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
        }

        item {
            InfoSpeachBubble(
                title = {
                    FadeInText(
                        delay = FadeInDelayPerIndex * 4,
                        text = "Thats HUGE!!!! \uD83D\uDCAA",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                message = {
                    FadeInText(
                        delay = FadeInDelayPerIndex * 3,
                        text = "You moved ${weightFormat(state.heaviestVariation.second)} in ${state.heaviestVariation.first}s Alone!! \uD83D\uDE35",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            )
        }
    }
}
