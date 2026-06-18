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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.submit_text_field_edit_content_description
import lift_bro.core.generated.resources.submit_text_field_save_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubmitTextField(
    value: String,
    placeholder: @Composable () -> Unit,
    onValueSubmitted: (String) -> Unit,
    editable: Boolean = false,
) {
    var currentValue by remember(value) { mutableStateOf(value) }
    var editable by remember(editable) { mutableStateOf(editable) }
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
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            singleLine = true,
            maxLines = 1,
            textStyle = MaterialTheme.typography.headlineMedium
                .copy(color = MaterialTheme.colorScheme.onBackground),
            keyboardActions = KeyboardActions(
                onAny = {
                    onValueSubmitted(currentValue)
                    editable = false
                }
            ),
            decorationBox = { inner ->
                when (editable) {
                    true -> {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.onBackground,

                        ) {
                            inner()
                        }
                    }

                    false -> Text(currentValue)
                }
                if (currentValue.isEmpty()) {
                    CompositionLocalProvider(
                        LocalContentColor provides LocalContentColor.current.copy(
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
                    contentDescription = stringResource(Res.string.submit_text_field_save_content_description)
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
                        contentDescription = stringResource(Res.string.submit_text_field_edit_content_description)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SubmitTextFieldFilledPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        SubmitTextField(
            value = "Bench Press",
            placeholder = { Text("Lift name") },
            onValueSubmitted = {},
            editable = false,
        )
    }
}

@Preview
@Composable
fun SubmitTextFieldEmptyPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        SubmitTextField(
            value = "",
            placeholder = { Text("Lift name") },
            onValueSubmitted = {},
            editable = false,
        )
    }
}

@Preview
@Composable
fun SubmitTextFieldEditablePreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        SubmitTextField(
            value = "",
            placeholder = { Text("Lift name") },
            onValueSubmitted = {},
            editable = true,
        )
    }
}
