package com.lift.bro.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp

fun Modifier.underlineRange(
    textLayoutResult: TextLayoutResult?,
    start: Int,
    end: Int,
    lineColor: Color,
): Modifier {
    return this.drawBehind {
        textLayoutResult?.getPathForRange(start, end)
            ?.getBounds()
            ?.let { bounds ->
                drawLine(
                    color = lineColor,
                    start = Offset(bounds.left, bounds.bottom),
                    end = Offset(bounds.right, bounds.bottom),
                    strokeWidth = 2.dp.toPx()
                )
            }
    }
}
