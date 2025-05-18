package com.lift.bro.presentation.set

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.decimalFormat

@Composable
fun RepWeightSelector(
    modifier: Modifier = Modifier,
    set: EditSetState,
    repChanged: (Long?) -> Unit,
    weightChanged: (Double?) -> Unit,
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            // TODO: figure out the accessibility here
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        RepWeightTextField(
            value = set.reps?.toString() ?: "",
            onValueChanged = {
                if (it.isBlank()) {
                    repChanged(null)
                } else {
                    it.toLongOrNull()?.let(repChanged)
                }
            },
            keyboardType = KeyboardType.Number
        )

        Space(MaterialTheme.spacing.half)

        Text(
            text = "x",
            style = MaterialTheme.typography.titleLarge,
        )
        Space(MaterialTheme.spacing.half)

        RepWeightTextField(
            value = set.weight.decimalFormat(),
            onValueChanged = {
                weightChanged(it.toDoubleOrNull())
            },
            keyboardType = KeyboardType.Decimal
        )

        Space(MaterialTheme.spacing.half)


        val uom by dependencies.settingsRepository.getUnitOfMeasure().collectAsState(null)
        Text(
            text = uom?.uom?.value ?: "",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun RepWeightTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChanged: (String) -> Unit,
    keyboardType: KeyboardType,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }

    var focusState by remember { mutableStateOf<FocusState?>(null) }

    BasicTextField(
        modifier = modifier.width(IntrinsicSize.Min)
            .onFocusEvent {
                focusState = it
                textFieldValue = if (it.isFocused) {
                    textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                } else {
                    textFieldValue.copy(
                        selection = TextRange.Zero
                    )
                }
            }
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.small,
                color = if (focusState?.isFocused == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
            ).padding(
                horizontal = MaterialTheme.spacing.half,
                vertical = MaterialTheme.spacing.half,
            ).defaultMinSize(
                minWidth = Dp.AccessibilityMinimumSize,
            ),
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            onValueChanged(it.text)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
        ),
        textStyle = MaterialTheme.typography.titleLarge.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground)
    )
}