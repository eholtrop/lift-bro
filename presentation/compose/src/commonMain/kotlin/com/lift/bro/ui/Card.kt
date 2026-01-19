package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

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
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.spacing.quarter),
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
            .padding(contentPadding)
    } else {
        modifier.clip(MaterialTheme.shapes.medium)
            .background(
                brush = backgroundBrush,
            )
            .padding(contentPadding)
    }

    Box(
        modifier = localModifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview
@Composable
fun CardPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one)
        ) {
            // Clickable card with solid color
            Card(
                modifier = Modifier.size(150.dp, 100.dp),
                onClick = {},
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "Clickable Card",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Space(MaterialTheme.spacing.one)

            // Non-clickable card with gradient
            Card(
                modifier = Modifier.size(150.dp, 100.dp),
                backgroundBrush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.surface
                    ),
                    start = Offset.Zero,
                    end = Offset(50f, 50f)
                )
            ) {
                Text(
                    "Gradient Card",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
