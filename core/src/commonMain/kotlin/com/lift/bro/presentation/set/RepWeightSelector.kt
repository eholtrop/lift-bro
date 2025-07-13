package com.lift.bro.presentation.set

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
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
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.decimalFormat

@Composable
fun RepWeightSelector(
    modifier: Modifier = Modifier,
    set: EditSetState,
    repChanged: (Long?) -> Unit,
    weightChanged: (Double?) -> Unit,
    rpeChanged: (Int?) -> Unit,
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

        Text(
            text = "${LocalUnitOfMeasure.current.value} at ",
            style = MaterialTheme.typography.titleLarge,
        )

        RepWeightTextField(
            value = set.rpe?.toString() ?: "",
            onValueChanged = {
                rpeChanged(it.toIntOrNull())
            },
            keyboardType = KeyboardType.Number,
            placeholder = {
                Text("RPE")
            }
        )

        InfoDialogButton(
            dialogTitle = { Text("RPE:\nRate of Perceived Exertion") },
            dialogMessage = {
                Column {
                    Text("A Scale that can be used to track the effort used for a given set")
                    Text("Can be calculated based on the \"Reps in Reserve\", % of max weight, or Vibes!")
                    Space(MaterialTheme.spacing.half)
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(
                                    color = Color.White,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(vertical = MaterialTheme.spacing.quarter),
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("RPE")
                                Text("10")
                                Text("9")
                                Text("8")
                                Text("7")
                                Text("6")
                                Text("5")
                                Text("1-4")
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("RIR")
                                Text("0")
                                Text("1")
                                Text("2")
                                Text("3")
                                Text("4")
                                Text("5-6")
                                Text("6+")
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("%")
                                Text("100")
                                Text("95")
                                Text("90")
                                Text("85")
                                Text("75")
                                Text("60")
                                Text("50")
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Vibe")
                                Text("\uD83D\uDC80")
                                Text("\uD83E\uDD75")
                                Text("\uD83D\uDE30")
                                Text("\uD83D\uDE05")
                                Text("\uD83D\uDCAA")
                                Text("\uD83D\uDE42")
                                Text("\uD83E\uDD29")
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun RepWeightTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChanged: (String) -> Unit,
    keyboardType: KeyboardType,
    placeholder: @Composable () -> Unit = {},
) {
    var textFieldValue by remember(value) { mutableStateOf(TextFieldValue(value)) }

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
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        decorationBox = { inner ->
            Box(
                contentAlignment = Alignment.Center,
            ) {
                inner()
                if (textFieldValue.text.isEmpty()) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            color = TextFieldDefaults.colors().focusedPlaceholderColor.copy(alpha = .6f),
                        )
                    ) {
                        placeholder()
                    }
                }
            }
        }
    )
}