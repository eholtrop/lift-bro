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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.decimalFormat
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.rep_weight_selector_info_p1
import lift_bro.core.generated.resources.rep_weight_selector_info_p2
import lift_bro.core.generated.resources.rep_weight_selector_info_title
import lift_bro.core.generated.resources.rep_weight_selector_rpe_placeholder
import lift_bro.core.generated.resources.rep_weight_selector_table_col_percent
import lift_bro.core.generated.resources.rep_weight_selector_table_col_rir
import lift_bro.core.generated.resources.rep_weight_selector_table_col_rpe
import lift_bro.core.generated.resources.rep_weight_selector_table_col_vibe
import lift_bro.core.generated.resources.rep_weight_selector_table_percent_100
import lift_bro.core.generated.resources.rep_weight_selector_table_percent_50
import lift_bro.core.generated.resources.rep_weight_selector_table_percent_60
import lift_bro.core.generated.resources.rep_weight_selector_table_percent_75
import lift_bro.core.generated.resources.rep_weight_selector_table_percent_85
import lift_bro.core.generated.resources.rep_weight_selector_table_percent_90
import lift_bro.core.generated.resources.rep_weight_selector_table_percent_95
import lift_bro.core.generated.resources.rep_weight_selector_table_rir_0
import lift_bro.core.generated.resources.rep_weight_selector_table_rir_1
import lift_bro.core.generated.resources.rep_weight_selector_table_rir_2
import lift_bro.core.generated.resources.rep_weight_selector_table_rir_3
import lift_bro.core.generated.resources.rep_weight_selector_table_rir_4
import lift_bro.core.generated.resources.rep_weight_selector_table_rir_5_6
import lift_bro.core.generated.resources.rep_weight_selector_table_rir_6_plus
import lift_bro.core.generated.resources.rep_weight_selector_table_rpe_10
import lift_bro.core.generated.resources.rep_weight_selector_table_rpe_1_4
import lift_bro.core.generated.resources.rep_weight_selector_table_rpe_5
import lift_bro.core.generated.resources.rep_weight_selector_table_rpe_6
import lift_bro.core.generated.resources.rep_weight_selector_table_rpe_7
import lift_bro.core.generated.resources.rep_weight_selector_table_rpe_8
import lift_bro.core.generated.resources.rep_weight_selector_table_rpe_9
import lift_bro.core.generated.resources.rep_weight_selector_times_symbol
import org.jetbrains.compose.resources.stringResource

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
            modifier = Modifier.testTag("reps"),
            value = set.reps?.toString(),
            error = set.reps == null,
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
            text = stringResource(Res.string.rep_weight_selector_times_symbol),
            style = MaterialTheme.typography.titleLarge,
        )
        Space(MaterialTheme.spacing.half)

        RepWeightTextField(
            modifier = Modifier.testTag("weight"),
            value = set.weight.decimalFormat(),
            error = set.weight == null,
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
            modifier = Modifier.testTag("rpe"),
            value = set.rpe?.toString() ?: "",
            onValueChanged = {
                rpeChanged(it.toIntOrNull())
            },
            keyboardType = KeyboardType.Number,
            placeholder = {
                Text(stringResource(Res.string.rep_weight_selector_rpe_placeholder))
            }
        )

        InfoDialogButton(
            dialogTitle = { Text(stringResource(Res.string.rep_weight_selector_info_title)) },
            dialogMessage = {
                Column {
                    Text(stringResource(Res.string.rep_weight_selector_info_p1))
                    Text(stringResource(Res.string.rep_weight_selector_info_p2))
                    Space(MaterialTheme.spacing.half)
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(vertical = MaterialTheme.spacing.quarter),
                        ) {
                            CompositionLocalProvider(

                                LocalContentColor provides Color.DarkGray,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(stringResource(Res.string.rep_weight_selector_table_col_rpe))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rpe_10))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rpe_9))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rpe_8))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rpe_7))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rpe_6))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rpe_5))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rpe_1_4))
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(stringResource(Res.string.rep_weight_selector_table_col_rir))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rir_0))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rir_1))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rir_2))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rir_3))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rir_4))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rir_5_6))
                                    Text(stringResource(Res.string.rep_weight_selector_table_rir_6_plus))
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(stringResource(Res.string.rep_weight_selector_table_col_percent))
                                    Text(stringResource(Res.string.rep_weight_selector_table_percent_100))
                                    Text(stringResource(Res.string.rep_weight_selector_table_percent_95))
                                    Text(stringResource(Res.string.rep_weight_selector_table_percent_90))
                                    Text(stringResource(Res.string.rep_weight_selector_table_percent_85))
                                    Text(stringResource(Res.string.rep_weight_selector_table_percent_75))
                                    Text(stringResource(Res.string.rep_weight_selector_table_percent_60))
                                    Text(stringResource(Res.string.rep_weight_selector_table_percent_50))
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(stringResource(Res.string.rep_weight_selector_table_col_vibe))
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
            }
        )
    }
}

@Composable
private fun RepWeightTextField(
    modifier: Modifier = Modifier,
    value: String?,
    error: Boolean = false,
    onValueChanged: (String) -> Unit,
    keyboardType: KeyboardType,
    placeholder: @Composable () -> Unit = {},
) {
    var focusState by remember { mutableStateOf<FocusState?>(null) }

    var textFieldValue by remember { mutableStateOf(TextFieldValue(value ?: "")) }

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
                color = when {
                    error -> MaterialTheme.colorScheme.error
                    focusState?.isFocused == true -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )
            .padding(
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
