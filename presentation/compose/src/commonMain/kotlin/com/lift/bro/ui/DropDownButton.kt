package com.lift.bro.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
