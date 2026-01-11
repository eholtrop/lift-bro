package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun RadioButtonCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.background(
            color = backgroundColor,
            shape = shape
        )
            .border(
                width = if (selected) 4.dp else 0.dp,
                color = Color.Black,
                shape = MaterialTheme.shapes.medium
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                onClick = onClick,
                role = Role.RadioButton
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview
@Composable
fun RadioButtonCardPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            RadioButtonCard(
                selected = true,
                onClick = {}
            ) {
                Text("Selected Card", modifier = Modifier.padding(MaterialTheme.spacing.one))
            }
            RadioButtonCard(
                selected = false,
                onClick = {}
            ) {
                Text("Unselected Card", modifier = Modifier.padding(MaterialTheme.spacing.one))
            }
        }
    }
}
