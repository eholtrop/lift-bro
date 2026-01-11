package com.lift.bro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    title: String,
    selectedNum: Int? = null,
    numberChanged: (Int?) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    textStyle: TextStyle = LocalTextStyle.current,
    suffix: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
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
        suffix = suffix,
        prefix = prefix,
        value = value,
        isError = selectedNum == null,
        onValueChange = {
            numberChanged(it.text.toIntOrNull())
            value = it
        },
        label = title.let {
            {
                Text(title)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction,
        ),
        textStyle = textStyle,
    )
}

@Preview
@Composable
fun NumberPickerPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            NumberPicker(
                title = "Reps",
                selectedNum = 5,
                numberChanged = {},
                imeAction = ImeAction.Done
            )
            NumberPicker(
                title = "Weight",
                selectedNum = null,
                numberChanged = {},
                imeAction = ImeAction.Done,
                suffix = { Text("lbs") }
            )
        }
    }
}
