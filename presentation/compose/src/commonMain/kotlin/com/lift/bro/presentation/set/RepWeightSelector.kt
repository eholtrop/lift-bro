package com.lift.bro.presentation.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
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
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialog
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
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
import lift_bro.core.generated.resources.rep_weight_selector_times_symbol
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import tv.dpal.compose.AccessibilityMinimumSize

@Preview
@Composable
fun RepWeightSelectorPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(
        isDarkMode = darkMode
    ) {
        RepWeightSelector(
            weight = 120.0,
            reps = 12L,
            rpe = 3,
            repChanged = {},
            weightChanged = {},
            rpeChanged = {},
            showRpe = true
        )
        RepWeightSelector(
            weight = 120.0,
            reps = 12L,
            rpe = 3,
            repChanged = {},
            weightChanged = {},
            rpeChanged = {},
            showRpe = false
        )
        RepWeightSelector(
            weight = null,
            reps = null,
            rpe = null,
            repChanged = {},
            weightChanged = {},
            rpeChanged = {},
            showRpe = true
        )
    }
}

@Composable
fun RepWeightSelector(
    modifier: Modifier = Modifier,
    weight: Double?,
    reps: Long?,
    rpe: Int?,
    showRpe: Boolean,
    repChanged: (Long?) -> Unit,
    weightChanged: (Double?) -> Unit,
    rpeChanged: (Int?) -> Unit,
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            // TODO: figure out the accessibility here
        }.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        RepWeightTextField(
            modifier = Modifier.testTag("reps"),
            value = reps?.toString(),
            error = reps == null,
            onValueChanged = {
                if (it.isBlank()) {
                    repChanged(null)
                } else {
                    it.toLongOrNull()?.let(repChanged)
                }
            },
            placeholder = { Text("0") },
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
            value = weight.decimalFormat(),
            error = weight == null,
            onValueChanged = {
                weightChanged(it.toDoubleOrNull())
            },
            placeholder = { Text("0.0") },
            keyboardType = KeyboardType.Decimal
        )

        if (showRpe) {
            Space(MaterialTheme.spacing.half)

            Text(
                text = "${LocalUnitOfMeasure.current.value} at",
                style = MaterialTheme.typography.titleLarge,
            )

            Space(MaterialTheme.spacing.half)

            RepWeightTextField(
                modifier = Modifier.testTag("rpe"),
                value = rpe?.toString() ?: "",
                onValueChanged = {
                    rpeChanged(it.toIntOrNull())
                },
                keyboardType = KeyboardType.Number,
                placeholder = {
                    Text(stringResource(Res.string.rep_weight_selector_rpe_placeholder))
                }
            )
            RpeInfoDialogButton()
        } else {
            Space(MaterialTheme.spacing.half)
            Text(
                text = LocalUnitOfMeasure.current.value,
                style = MaterialTheme.typography.titleLarge,
            )
            if (LocalTwmSettings.current) {
                val twm = weight?.times(reps ?: 0L)

                AnimatedVisibility(
                    twm != null && twm > 0,
                    enter = slideInHorizontally { it },
                    exit = fadeOut(),
//                    exit = slideOutHorizontally { it }
                ) {
                    Text(
                        text = " = ${twm.decimalFormat()}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun RepInfoDialogMessage() {
    val sortedRpe = remember { RPE.entries.sortedByDescending { it.rpe } }
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
                        sortedRpe.forEach { rpe ->
                            Text(rpe.rpe.toString())
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.rep_weight_selector_table_col_rir))
                        sortedRpe.forEachIndexed { index, rpe ->
                            Text("${rpe.rir}${if (index == RPE.entries.size - 1) "+" else ""}")
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.rep_weight_selector_table_col_percent))
                        sortedRpe.forEachIndexed { index, rpe ->
                            Text((rpe.percentMax * 100).toInt().toString())
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.rep_weight_selector_table_col_vibe))
                        RPE.entries.sortedByDescending { it.rpe }.forEachIndexed { index, rpe ->
                            Text(rpe.emoji)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RpeInfoDialogButton() {
    InfoDialogButton(
        dialogTitle = { Text(stringResource(Res.string.rep_weight_selector_info_title)) },
        dialogMessage = {
            RepInfoDialogMessage()
        }
    )
}

@Composable
fun RpeInfoDialog(
    onDismissRequest: () -> Unit,
) {
    InfoDialog(
        title = { Text(stringResource(Res.string.rep_weight_selector_info_title)) },
        message = { RepInfoDialogMessage() },
        onDismissRequest = onDismissRequest,
    )
}

data class RepWeightColorDefaults(
    val error: Color,
    val focused: Color,
    val default: Color,
)

@Composable
fun TextFieldDefaults.repWeightColorDefaults() = RepWeightColorDefaults(
    error = MaterialTheme.colorScheme.error,
    focused = MaterialTheme.colorScheme.secondary,
    default = MaterialTheme.colorScheme.onBackground,
)

@Composable
private fun RepWeightTextField(
    modifier: Modifier = Modifier,
    value: String?,
    error: Boolean = false,
    onValueChanged: (String) -> Unit,
    keyboardType: KeyboardType,
    colors: RepWeightColorDefaults = TextFieldDefaults.repWeightColorDefaults(),
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
                    error -> colors.error
                    focusState?.isFocused == true -> colors.focused
                    else -> colors.default
                }
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small
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
