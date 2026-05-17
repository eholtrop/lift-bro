package com.lift.bro.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@Composable
fun SubmitTextField(
    value: String,
    placeholder: @Composable () -> Unit,
    onValueSubmitted: (String) -> Unit,
    colors: TextFieldColors = TextFieldDefaults.colors(),
) {
    var currentValue by remember(value) { mutableStateOf(value) }
    var editable by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            modifier = Modifier
                .focusRequester(focusRequester),
            value = currentValue,
            onValueChange = {
                currentValue = it
            },
            singleLine = true,
            maxLines = 1,
            textStyle = MaterialTheme.typography.headlineMedium,
            keyboardActions = KeyboardActions(
                onAny = {
                    onValueSubmitted(currentValue)
                    editable = false
                }
            ),
            decorationBox = { inner ->
                // Rendering Text
                when (editable) {
                    true -> { inner() }
                    false -> Text(currentValue)
                }
                if (currentValue.isEmpty()) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onBackground.copy(
                            alpha = .8f
                        )
                    ) {
                        placeholder()
                    }
                }
            }
        )
        val keyboard = LocalSoftwareKeyboardController.current
        when (editable) {
            true -> IconButton(
                onClick = {
                    editable = false
                    focusRequester.freeFocus()
                    onValueSubmitted(currentValue)
                    keyboard?.hide()
                },
                enabled = currentValue.isNotEmpty(),
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save"
                )
            }

            false -> {
                IconButton(
                    onClick = {
                        editable = true
                        focusRequester.requestFocus()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
            }
        }
    }
}
