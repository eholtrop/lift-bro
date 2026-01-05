package com.lift.bro.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorBarBell(
    visible: Boolean,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally { -it } + fadeIn(),
        exit = slideOutHorizontally { -it } + fadeOut()
    ) {
        Box(
            modifier = Modifier.animateContentSize().background(
                color = Color.Gray,
                shape = MaterialTheme.shapes.small.copy(
                    topStart = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp)
                )
            )
                .fillMaxWidth(.75f)
                .height(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
        }
    }
}
