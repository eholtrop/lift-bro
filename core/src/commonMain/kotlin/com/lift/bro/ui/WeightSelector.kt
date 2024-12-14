package com.lift.bro.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.set.DecimalPicker
import com.lift.bro.presentation.set.NumberPicker
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.variation.UOM
import com.lift.bro.presentation.variation.formattedTempo
import com.lift.bro.presentation.variation.formattedWeight
import com.lift.bro.presentation.variation.render
import kotlinx.coroutines.Job

expect object DecimalFormat {
    fun formatWeight(weight: Double?): String
}

@Composable
fun WeightSelector(
    modifier: Modifier = Modifier,
    liftId: String,
    weight: Pair<Double?, UOM>,
    placeholder: String,
    weightChanged: (Pair<Double?, UOM>) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var currentValue by remember { mutableStateOf(DecimalFormat.formatWeight(weight.first)) }

        DecimalPicker(
            modifier = Modifier.weight(.33f).height(52.dp),
            title = placeholder,
            selectedNum = currentValue.toDoubleOrNull(),
            numberChanged = {
                currentValue = DecimalFormat.formatWeight(it)
                weightChanged(weight.copy(first = it))
            },
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