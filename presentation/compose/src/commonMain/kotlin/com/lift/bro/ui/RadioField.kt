package com.lift.bro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun RadioField(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    fieldSelected: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = selected,
                onClick = fieldSelected,
                role = Role.RadioButton
            )
            .padding(horizontal = MaterialTheme.spacing.one)
            .minimumInteractiveComponentSize()
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )

        Space(MaterialTheme.spacing.one)

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
fun RadioFieldPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            RadioField(
                text = "Selected option",
                selected = true,
                fieldSelected = {}
            )
            RadioField(
                text = "Unselected option",
                selected = false,
                fieldSelected = {}
            )
        }
    }
}
