package com.lift.bro.presentation.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.ui.AnimatedText
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import kotlinx.datetime.Instant
import tv.dpal.ext.ktx.datetime.toString

@Composable
fun RunningTimerContent(
    modifier: Modifier = Modifier,
    state: TimerState.Running,
    onEvent: (TimerEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.currentTimer?.let { timer ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    timer.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                Space(MaterialTheme.spacing.half)
                val remainingTime = (timer.totalTime - timer.elapsedTime) / 1000.0

                val remaining = Instant.fromEpochMilliseconds(timer.totalTime - timer.elapsedTime)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val textStyle = MaterialTheme.typography.displayMedium
                    val textModifier = textModifier()
                    AnimatedText(
                        modifier = textModifier,
                        style = textStyle,
                        text = remaining.toString("ss").take(1),
                        color = if (remainingTime <= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Space(MaterialTheme.spacing.eighth)
                    AnimatedText(
                        modifier = textModifier,
                        style = textStyle,
                        text = remaining.toString("ss").drop(1),
                        color = if (remainingTime <= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Space(MaterialTheme.spacing.quarter)
                    Text(":")
                    Space(MaterialTheme.spacing.quarter)
                    Text(
                        modifier = textModifier,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        text = remaining.toString("SS").take(1)
                    )
                    Space(MaterialTheme.spacing.eighth)
                    Text(
                        modifier = textModifier,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        text = remaining.toString("SS").drop(1)
                    )
                }
            }
        }
    }
}
