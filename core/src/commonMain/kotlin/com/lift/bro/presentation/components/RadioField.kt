package com.lift.bro.presentation.components

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
import com.lift.bro.presentation.spacing
import com.lift.bro.ui.Space

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