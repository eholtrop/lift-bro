package com.lift.bro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme

@Composable
fun DecimalPicker(
    modifier: Modifier = Modifier,
    title: String? = null,
    selectedNum: Double? = null,
    numberChanged: (Double?) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
) {
    var value by remember { mutableStateOf(TextFieldValue(selectedNum?.toString() ?: "")) }

    var focus by remember { mutableStateOf(false) }

    if (focus) {
        LaunchedEffect(focus) {
            if (focus) {
                value = value.copy(selection = TextRange(0, value.text.length))
            }
        }
    }

    TextField(
        modifier = modifier.onFocusChanged {
            focus = it.isFocused
        },
        value = value,
        onValueChange = {
            numberChanged(it.text.toDoubleOrNull())
            value = it
        },
        label = {
            title?.let {
                Text(title)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        prefix = prefix,
        suffix = suffix,
    )
}

@Preview
@Composable
fun DecimalPickerPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            DecimalPicker(
                title = "Weight",
                selectedNum = 135.5,
                numberChanged = {},
                imeAction = ImeAction.Done,
                suffix = { Text("kg") }
            )
            DecimalPicker(
                title = "Empty",
                selectedNum = null,
                numberChanged = {},
                imeAction = ImeAction.Done
            )
        }
    }
}
