package com.lift.bro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    title: String,
) {
    Box(
        modifier = modifier.defaultMinSize(minHeight = 128.dp)
            .fillMaxWidth()
    ) {
        ToolbarTitle(
            modifier = Modifier.align(Alignment.BottomCenter),
            title = title,
        )
    }

}

@Composable
fun ToolbarTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        modifier = modifier,
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineLarge,
    )

}