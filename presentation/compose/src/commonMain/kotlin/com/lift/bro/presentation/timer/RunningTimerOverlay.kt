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
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import org.jetbrains.compose.ui.tooling.preview.Preview
import tv.dpal.ext.ktx.datetime.toString

@Composable
fun RunningTimerContent(
    modifier: Modifier = Modifier,
    state: TimerState.Running,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        state.currentTimer?.let { timer ->
            Space(MaterialTheme.spacing.two)
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
                val textStyle = MaterialTheme.typography.displayLarge
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

@Preview
@Composable
fun TimerRunning2Screen_Preview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState.Running(
                elapsedTime = 1,
                timers = listOf(
                    TimerSegment(
                        name = "Setup",
                        elapsedTime = 0,
                        totalTime = 5000,
                    ),
                    TimerSegment(
                        name = "Ecc",
                        elapsedTime = 0,
                        totalTime = 3000,
                    ),
                    TimerSegment(
                        name = "Iso",
                        elapsedTime = 0,
                        totalTime = 1000,
                    ),
                    TimerSegment(
                        name = "Con",
                        elapsedTime = 0,
                        totalTime = 1000,
                    ),
                    TimerSegment(
                        name = "Rest",
                        elapsedTime = 0,
                        totalTime = 3000
                    )
                ),
                paused = false,
                lastTickTime = Clock.System.now().minus(3, DateTimeUnit.SECOND),
                beep = false,
                set = null,
            ),
            onEvent = {},
        )
    }
}

