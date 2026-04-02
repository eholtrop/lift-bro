@file:OptIn(ExperimentalFoundationApi::class)

package com.lift.bro.presentation.lift

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.theme.spacing
import io.ktor.client.request.invoke
import kotlinx.datetime.LocalDate
import kotlin.math.max
import kotlin.math.min

data class DotGraphColors(
    val dotColor: Color,
    val dotColorSelected: Color,
)

data class DotGraphData(
    val x: LocalDate,
    val y: Float,
)

data class LineGraphData(
    val x: LocalDate,
    val y: Float,
)

data class GraphData(
    val date: LocalDate,
    val dotGraphData: Float,
    val lineGraphData: Float,
)

@Composable
fun DotGraph(
    modifier: Modifier = Modifier,
    graphData: List<Pair<LocalDate, GraphData>>,
    state: LazyListState = rememberLazyListState(),
    selectedData: LocalDate? = null,
    maxX: LocalDate = graphData.maxOf { it.first },
    maxY: Float = graphData.maxOf { it.second.dotGraphData },
    xAxis: @Composable ((Long) -> Unit)? = null,
    yAxis: @Composable ((Float, Float) -> Unit)? = null,
    dataPointClicked: ((LocalDate) -> Unit)? = null,
    colors: DotGraphColors,
) {
    Row(
        modifier = modifier
    ) {
        LazyRow(
            modifier = modifier.weight(1f),
            state = state,
            reverseLayout = true,
        ) {
            itemsIndexed(
                items = graphData
            ) { index, point ->
                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 52.dp)
                        .clickable(
                            enabled = dataPointClicked != null,
                            onClick = { dataPointClicked?.invoke(point.first) },
                            role = Role.Button
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val previousPoint = graphData.getOrNull(index - 1)
                    val nextPoint = graphData.getOrNull(index + 1)

                    val dotColor =
                        if (selectedData == point.first) colors.dotColorSelected else colors.dotColor
                    val lineColor = dotColor

                    var canvasSize by remember { mutableStateOf(Size.Zero) }
                    var targetDotOffset by remember(canvasSize) {
                        mutableStateOf(
                            Offset(
                                canvasSize.width / 2,
                                0f,
                            )
                        )
                    }

                    val animateDotOffset by animateOffsetAsState(
                        targetValue = targetDotOffset
                    )

                    var barHeight by rememberSaveable { mutableStateOf(0f) }
                    val animatedHeight by animateFloatAsState(
                        targetValue = barHeight
                    )

                    Canvas(
                        modifier = Modifier
                            .width(52.dp)
                            .weight(1f)
                    ) {
                        canvasSize = size
                        targetDotOffset =
                            targetDotOffset.copy(
                                y = size.height - (point.second.dotGraphData.div(max(maxY, 1f)) * size.height)
                            )
                        drawCircle(
                            color = dotColor,
                            radius = MaterialTheme.spacing.quarter.toPx(),
                            center = animateDotOffset
                        )

                        nextPoint?.let { pp ->
                            val y = (point.second.dotGraphData + pp.second.dotGraphData) / 2
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, size.height - (y.div(max(maxY, 1f)) * size.height)),
                                end = Offset(size.width / 2f, animateDotOffset.y)
                            )
                        }

                        previousPoint?.let { pp ->
                            val y = (point.second.dotGraphData + pp.second.dotGraphData) / 2
                            drawLine(
                                color = lineColor,
                                start = Offset(size.width / 2f, animateDotOffset.y),
                                end = Offset(size.width, size.height - (y.div(max(maxY, 1f)) * size.height))
                            )
                        }

                        barHeight = min(size.height, point.second.lineGraphData * size.height)
                        drawRect(
                            color = dotColor.copy(alpha = .6f),
                            size = Size(size.width, animatedHeight),
                            topLeft = Offset(0f, size.height - animatedHeight)
                        )
                    }

                    xAxis?.invoke(point.first.toEpochDays().toLong())
                }
            }
        }

        yAxis?.invoke(
            graphData.minOf { it.second.dotGraphData },
            graphData.minOf { it.second.dotGraphData }
        )
    }
}
