package tv.dpal.compose.horizontal_padding

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.padding(
    start: Dp = 0.dp,
    end: Dp = 0.dp,
    vertical: Dp = 0.dp,
): Modifier = padding(
    start = start,
    top = vertical,
    end = end,
    bottom = vertical,
)
