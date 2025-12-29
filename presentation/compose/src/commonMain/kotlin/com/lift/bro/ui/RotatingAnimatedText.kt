package com.lift.bro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

@Composable
fun AnimatedRotatingText(
    modifier: Modifier = Modifier,
    text: List<String>,
    startIndex: Int = if (text.isNotEmpty()) text.indexOf(text.random()) else 0,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
) {
    if (text.isEmpty()) return

    var currentIndex by remember { mutableStateOf(startIndex) }
    var visibility by remember { mutableStateOf(true) }

    AnimatedVisibility(
        modifier = modifier,
        visible = visibility,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        Text(
            text = text[currentIndex],
            style = style,
            color = color,
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            visibility = false
            delay(1100)
            if (currentIndex == text.lastIndex) {
                currentIndex = 0
            } else {
                currentIndex ++
            }
            visibility = true
            delay(5000)
        }
    }
}
