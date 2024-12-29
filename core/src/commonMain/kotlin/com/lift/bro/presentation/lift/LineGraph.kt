@file:OptIn(ExperimentalFoundationApi::class)

package com.lift.bro.presentation.lift

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.spacing

@Composable
fun DotGraph(
    modifier: Modifier = Modifier,
    data: List<DotGraphData>,
    state: LazyListState = rememberLazyListState(),
    selectedData: DotGraphData? = null,
    maxX: Long = data.maxOf { it.x },
    maxY: Float = data.maxOf { it.y },
    xAxis: @Composable ((Long) -> Unit)? = null,
    yAxis: @Composable ((Float, Float) -> Unit)? = null,
    dataPointClicked: ((DotGraphData) -> Unit)? = null,
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
                items = data
            ) { index, point ->
                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 52.dp)
                        .clickable(
                            enabled = dataPointClicked != null,
                            onClick = { dataPointClicked?.invoke(point) },
                            role = Role.Button
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    val dotColor =
                        if (selectedData == point) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                    Canvas(
                        modifier = Modifier
                            .width(52.dp)
                            .weight(1f)
                    ) {
                        val center = Offset(size.width / 2f, size.height - (point.y.div(maxY) * size.height))

                        drawCircle(
                            color = dotColor,
                            radius = MaterialTheme.spacing.half.toPx(),
                            center = center,
                        )
                    }


                    xAxis?.invoke(point.x)
                }
            }
        }

        yAxis?.invoke(
            data.minOf { it.y },
            data.maxOf { it.y }
        )

    }
}

data class DotGraphData(
    val x: Long,
    val y: Float
)