package com.lift.bro.presentation.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.AnimatedText
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme

@Composable
fun PlanTimerOverlay(
    modifier: Modifier = Modifier,
    state: TimerState.Plan,
    onEvent: (TimerEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(state.tempo.any { t -> t != state.tempo.firstOrNull() }) }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            all = MaterialTheme.spacing.one,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            TextField(
                modifier = textModifier().width(96.dp),
                value = state.startupTime.toString(),
                onValueChange = {
                    it.toLongOrNull()?.let {
                        onEvent(TimerEvent.Plan.StartupTimeChanged(it))
                    }
                },
                label = { Text("Ready") },
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    textAlign = TextAlign.Center,
                ),
                colors = TextFieldDefaults.transparentColors(),
            )
        }

        item {
            Icon(
                modifier = Modifier.fillMaxWidth(),
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null
            )
        }

        item {
            val tempo = state.tempo.first()
            Row(
                modifier = Modifier.animateItem(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimerSetField(
                    state = tempo,
                    rest = state.perSetRest,
                    rep = if (!expanded) null else 0,
                    onEvent = onEvent,
                )
                if (!expanded) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Space(MaterialTheme.spacing.threeQuarters)
                        IconButton(
                            modifier = Modifier.height(MaterialTheme.spacing.two),
                            onClick = {
                                onEvent(TimerEvent.Plan.AddTimer)
                            }
                        ) {
                            Text("+")
                        }
                        Button(
                            onClick = {
                                expanded = true
                            },
                            shape = MaterialTheme.shapes.extraSmall,
                            colors = ButtonDefaults.textButtonColors(),
                            contentPadding = PaddingValues(
                                start = MaterialTheme.spacing.quarter,
                                end = MaterialTheme.spacing.quarter
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.quarter),
                                    text = "x",
                                    style = MaterialTheme.typography.displaySmall,
                                )
                                AnimatedText(
                                    modifier = Modifier.weight(1f),
                                    text = state.tempo.size.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                )
                            }
                        }
                        IconButton(
                            modifier = Modifier.height(MaterialTheme.spacing.two),
                            onClick = {
                                onEvent(TimerEvent.Plan.RemoveTimer())
                            },
                            enabled = state.tempo.size > 1
                        ) {
                            Text("-")
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            onEvent(TimerEvent.Plan.RemoveTimer(0))
                        },
                        enabled = state.tempo.size > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Tempo",
                        )
                    }
                }
            }
        }

        if (expanded && state.tempo.isNotEmpty()) {
            itemsIndexed(
                state.tempo.drop(1),
            ) { i, tempo ->
                // increase index to match state since we dropped 1
                val index = i + 1
                Row(
                    modifier = Modifier.wrapContentHeight(),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TimerSetField(
                                rest = state.perSetRest,
                                rep = index,
                                state = tempo,
                                onEvent = onEvent
                            )
                            IconButton(
                                onClick = {
                                    onEvent(TimerEvent.Plan.RemoveTimer(index))
                                },
                                enabled = state.tempo.size > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Tempo",
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            if (expanded) {
                Row {
                    IconButton(
                        onClick = {
                            onEvent(TimerEvent.Plan.AddTimer)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tempo"
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                )
                Space(MaterialTheme.spacing.one)
                Text(
                    text = "\uD83D\uDE2E\u200D\uD83D\uDCA8",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun TimerScreenPlanOverlayPreview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState.Plan(
                tempo = listOf(Tempo(), Tempo(), Tempo())
            ),
            onEvent = {},
        )
    }
}

@Preview
@Composable
fun TimerScreenPlanOverlay_Preview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState.Plan(
                tempo = listOf(Tempo(down = 10), Tempo(), Tempo())
            ),
            onEvent = {},
        )
    }
}
