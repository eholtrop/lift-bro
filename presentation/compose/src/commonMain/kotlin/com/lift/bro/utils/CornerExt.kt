package com.lift.bro.utils

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun CornerBasedShape.listCorners(index: Int, items: List<Any>) = this.copy(
    topStart = if (index == 0) MaterialTheme.shapes.large.topStart else CornerSize(0.dp),
    topEnd = if (index == 0) MaterialTheme.shapes.large.topEnd else CornerSize(0.dp),
    bottomEnd = if (index == items.lastIndex) MaterialTheme.shapes.large.bottomEnd else CornerSize(0.dp),
    bottomStart = if (index == items.lastIndex) MaterialTheme.shapes.large.bottomStart else CornerSize(0.dp),
)
