package com.lift.bro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme

@Composable
fun DropDownButton(
    modifier: Modifier = Modifier,
    buttonText: String,
    content: @Composable ColumnScope.() -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    Button(
        modifier = modifier,
        onClick = {
            showDropdown = true
        },
        colors = ButtonDefaults.textButtonColors()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buttonText
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null
            )
        }
    }
    DropdownMenu(
        expanded = showDropdown,
        onDismissRequest = {
            showDropdown = false
        },
        content = content,
    )
}

@Preview
@Composable
fun DropDownButtonPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            DropDownButton(
                buttonText = "Select Option"
            ) {
                DropdownMenuItem(
                    text = { Text("Option 1") },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("Option 2") },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("Option 3") },
                    onClick = {}
                )
            }
        }
    }
}
