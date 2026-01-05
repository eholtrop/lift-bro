package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.lift.bro.ui.theme.spacing

@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    val localModifier = if (onClick != null) {
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .background(
                color = backgroundColor,
            )
            .padding(MaterialTheme.spacing.quarter)
    } else {
        modifier.clip(MaterialTheme.shapes.medium)
            .background(
                color = backgroundColor,
            )
            .padding(MaterialTheme.spacing.quarter)
    }

    Box(
        modifier = localModifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundBrush: Brush,
    content: @Composable () -> Unit,
) {
    val localModifier = if (onClick != null) {
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .background(
                brush = backgroundBrush,
            )
            .padding(MaterialTheme.spacing.quarter)
    } else {
        modifier.clip(MaterialTheme.shapes.medium)
            .background(
                brush = backgroundBrush,
            )
            .padding(MaterialTheme.spacing.quarter)
    }

    Box(
        modifier = localModifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
