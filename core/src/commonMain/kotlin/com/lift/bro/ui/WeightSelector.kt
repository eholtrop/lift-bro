package com.lift.bro.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.lift.bro.presentation.variation.UOM
import spacing

@Composable
fun WeightSelector(
    modifier: Modifier = Modifier,
    weight: Pair<Double?, UOM>,
    placeholder: String,
    weightChanged: (Pair<Double?, UOM>) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            modifier = modifier,
            value = weight.first?.toString() ?: "",
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
            label = {
                Text(placeholder)
            },
            onValueChange = {
                if (it.toDoubleOrNull() != null) {
                    weightChanged(weight.copy(first = it.toDoubleOrNull()))
                }
            }
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.one))

        Button(
            onClick = {
                if (weight.second == UOM.POUNDS) {
                    weightChanged(
                        weight.copy(second = UOM.KG)
                    )
                } else {
                    weightChanged(
                        weight.copy(second = UOM.POUNDS)
                    )
                }
            }
        ) {
            Text(text = weight.second.value)
        }
    }
}