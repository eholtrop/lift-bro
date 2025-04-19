package com.lift.bro.ui

import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun DecimalPicker(
    modifier: Modifier,
    title: String,
    selectedNum: Double? = null,
    numberChanged: (Double?) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
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
            Text(title)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        suffix = suffix,
    )
}