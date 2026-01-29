package tv.dpal.compose.vertical_padding

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.padding(
    horizontal: Dp = 0.dp,
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
): Modifier = padding(
    start = horizontal,
    top = top,
    end = horizontal,
    bottom = bottom,
)
