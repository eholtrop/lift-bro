package com.lift.bro.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.variation.UOM
import com.lift.bro.presentation.variation.formattedTempo
import com.lift.bro.presentation.variation.formattedWeight

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

    var referenceSet by remember { mutableStateOf<LBSet?>(null) }

    Column(
        modifier = modifier.animateContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            var currentValue by remember { mutableStateOf(DecimalFormat.formatWeight(weight.first)) }

            TextField(
                value = currentValue,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                label = {
                    Text(placeholder)
                },
                onValueChange = {
                    currentValue = it
                    if (it.toDoubleOrNull() != null) {
                        weightChanged(weight.copy(first = it.toDoubleOrNull()))
                    }
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

        val variations = dependencies.database.variantDataSource.getAll(liftId = liftId)

        val sets = variations.map { dependencies.database.setDataSource.getAll(it.id) }
            .fold(emptyList<LBSet>()) { list, sets -> list + sets }
            .sortedBy { it.reps }

        Space(MaterialTheme.spacing.one)

        if (sets.isNotEmpty()) {
            Text(
                text = "Recent Sets"
            )

            Space(MaterialTheme.spacing.half)

            LazyRow {
                items(sets) { set ->
                    Card(
                        modifier = Modifier.padding(end = MaterialTheme.spacing.half),
                        onClick = { referenceSet = set }
                    ) {
                        Column(
                            modifier = Modifier.padding(MaterialTheme.spacing.half)
                        ) {
                            Text(
                                text = variations.firstOrNull { it.id == set.variationId }?.name
                                    ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${set.reps} x ${set.formattedWeight}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = set.formattedTempo,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}