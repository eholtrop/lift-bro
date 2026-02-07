package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme

@Composable
fun LineItem(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .clickable(
                enabled = true,
                onClick = onClick,
                role = Role.Button,
            )
            .padding(
                vertical = MaterialTheme.spacing.quarter,
                horizontal = MaterialTheme.spacing.one
            )
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        title?.let {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun LineItemPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            LineItem(
                title = "Title",
                description = "Description text goes here",
                onClick = {}
            )
            LineItem(
                description = "Description only (no title)",
                onClick = {}
            )
        }
    }
}
