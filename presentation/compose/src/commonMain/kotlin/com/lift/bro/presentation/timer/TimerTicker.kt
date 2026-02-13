package com.lift.bro.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun TimerTrack(
    modifier: Modifier = Modifier,
    segments: List<TimerSegment> = emptyList(),
    scrollable: Boolean,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        val totalTime by remember(segments) { derivedStateOf { segments.sumOf { it.totalTime } } }
        val elapsedTime by derivedStateOf { segments.sumOf { it.elapsedTime } }

        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        Box {
            LazyRow(
                state = listState,
                userScrollEnabled = scrollable,
                contentPadding = PaddingValues(
                    start = MaterialTheme.spacing.oneAndHalf,
                )
            ) {
                itemsIndexed(items = segments) { index, timer ->
                    val timerWidth = timer.totalTime.div(1000).times(16).toInt()

                    val density = LocalDensity.current
                    SideEffect {
                        if (timer.elapsedTime > 0 && timer.elapsedTime <= timer.totalTime) {
                            coroutineScope.launch {
                                listState.scrollToItem(
                                    index,
                                    (timerWidth.dp.value * (timer.elapsedTime / timer.totalTime.toFloat()) * density.density).toInt()
                                )
                            }
                        }
                    }

                    TimerTrackSegment(
                        modifier = Modifier.width(timerWidth.dp),
                        index = index,
                        timer = timer,
                    )
                }
            }

            // snapshot the remaining time when the list cannot be scrolled forward
            val remainingTime by remember(
                listState.canScrollForward
            ) { mutableStateOf(max(totalTime - elapsedTime, 1L)) }

            var lineSize by remember { mutableStateOf(Size(0f, 0f)) }
            val linePadding = MaterialTheme.spacing.one.minus(MaterialTheme.spacing.eighth)
            val lineTranslationTotal = listState.layoutInfo.viewportSize.width - lineSize.width - linePadding.value
            Column(
                modifier = Modifier.padding(horizontal = linePadding)
                    .onSizeChanged {
                        lineSize = Size(it.width.toFloat(), it.height.toFloat())
                    }
                    .graphicsLayer(
                        translationX = if (listState.canScrollForward) {
                            0f
                        } else {
                            lineTranslationTotal -
                                (lineTranslationTotal * ((totalTime - elapsedTime) / remainingTime.toFloat()))
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("\uD83D\uDCAA")
                Surface(
                    modifier = Modifier.height(44.dp)
                        .width(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 3.dp
                ) {}
            }
        }
        TimerProgressBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.one.minus(3.dp))
                .height(12.dp),
            progress = elapsedTime.toFloat() / totalTime
        )
    }
}

@Composable
fun TimerTrackSegment(
    index: Int,
    timer: TimerSegment,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = (timer.totalTime / 1000).toString() + "s")

        Box(
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.background(
                    color = when (index % 3) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
                    .height(32.dp)
                    .fillMaxWidth()
            )
            Row {
                if (index != 0) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            .height(44.dp)
                            .width(1.dp)
                    )
                }

                Space()

                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        .height(44.dp)
                        .width(1.dp)
                )
            }
        }
    }
}

@Composable
fun TimerProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
) {
    LinearProgressIndicator(
        modifier = modifier,
        progress = { progress }
    )
}
