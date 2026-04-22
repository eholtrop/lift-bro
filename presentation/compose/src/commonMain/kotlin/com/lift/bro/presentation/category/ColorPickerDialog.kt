package com.lift.bro.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.color_picker_dialog_blue
import lift_bro.core.generated.resources.color_picker_dialog_green
import lift_bro.core.generated.resources.color_picker_dialog_red
import lift_bro.core.generated.resources.color_picker_dialog_title
import lift_bro.core.generated.resources.color_picker_negative_cta
import lift_bro.core.generated.resources.color_picker_positive_cta
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    color: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        val controller = rememberColorPickerController()
        var selectedColor by remember { mutableStateOf(color) }

        Column(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
            ).padding(MaterialTheme.spacing.one),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.color_picker_dialog_title),
                style = MaterialTheme.typography.titleLarge
            )

            Space(MaterialTheme.spacing.one)

            HsvColorPicker(
                modifier = Modifier.size(
                    256.dp,
                    256.dp
                ),
                controller = controller,
                onColorChanged = {
                    selectedColor = it.color
                },
                initialColor = selectedColor
            )

            Space(MaterialTheme.spacing.two)

            Box(
                modifier = Modifier.background(
                    color = selectedColor,
                    shape = CircleShape,
                )
                    .border(
                        1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape
                    )
                    .size(32.dp),
                content = {}
            )

            Space(MaterialTheme.spacing.two)

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
            ) {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = (selectedColor.red * 255).toInt().toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let {
                            selectedColor = selectedColor.copy(red = it / 255f)
                        }
                    },
                    label = {
                        Text(stringResource(Res.string.color_picker_dialog_red))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                    ),
                )

                TextField(
                    modifier = Modifier.weight(1f),
                    value = (selectedColor.green * 255).toInt().toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let {
                            selectedColor = selectedColor.copy(green = it / 255f)
                        }
                    },
                    label = {
                        Text(stringResource(Res.string.color_picker_dialog_green))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                    ),
                )

                TextField(
                    modifier = Modifier.weight(1f),
                    value = (selectedColor.blue * 255).toInt().toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let {
                            selectedColor = selectedColor.copy(blue = it / 255f)
                        }
                    },
                    label = {
                        Text(stringResource(Res.string.color_picker_dialog_blue))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                    ),
                )
            }

            Space(MaterialTheme.spacing.one)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(stringResource(Res.string.color_picker_negative_cta))
                }

                Space(MaterialTheme.spacing.half)

                Button(
                    onClick = {
                        onColorSelected(selectedColor)
                    },
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(stringResource(Res.string.color_picker_positive_cta))
                }
            }
        }
    }
}
