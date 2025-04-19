package com.lift.bro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.fullName
import com.lift.bro.ui.theme.spacing
import com.lift.bro.presentation.variation.UOM
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.weight_selector_chin_subtitle
import lift_bro.core.generated.resources.weight_selector_chin_title
import org.jetbrains.compose.resources.stringResource

expect object DecimalFormat {
    fun formatWeight(weight: Double?): String
}

@Composable
fun WeightSelector(
    modifier: Modifier = Modifier,
    variation: Variation,
    weight: Pair<Double?, UOM>,
    placeholder: String,
    weightChanged: (Pair<Double?, UOM>) -> Unit,
) {
    val sets = dependencies.database.setDataSource.getAllForLift(variation.lift?.id ?: "")
    val liftMax = sets.maxByOrNull { it.weight }
    val variationMax = sets.filter { it.variationId == variation.id }.maxByOrNull { it.weight }



    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var percentageDenominator by remember { mutableStateOf(liftMax ?: variationMax) }

        var currentValue by remember {
            mutableStateOf(DecimalFormat.formatWeight(weight.first))
        }


        DecimalPicker(
            modifier = Modifier.fillMaxWidth(),
            title = placeholder,
            selectedNum = currentValue.toDoubleOrNull(),
            numberChanged = {
                currentValue = DecimalFormat.formatWeight(it)
                weightChanged(weight.copy(first = it))
            },
            suffix = {
                val uom by dependencies.settingsRepository.getUnitOfMeasure().collectAsState(null)
                uom?.let {
                    Text(it.uom.value)
                }
            },
        )

        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small.copy(
                topStart = CornerSize(0.dp),
                topEnd = CornerSize(0.dp),
            )
        ) {
            Box(
                modifier = Modifier.padding(
                    vertical = MaterialTheme.spacing.half, horizontal = MaterialTheme.spacing.one
                )
            ) {
                weight.first?.let { weight ->
                    Column {
                        if (liftMax != null) {
                            val percentage = ((weight / liftMax.weight) * 100).toInt()
                            Text(
                                text = stringResource(
                                    Res.string.weight_selector_chin_title,
                                    percentage,
                                    variation.lift?.name ?: "",
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (variationMax != null) {
                            val percentage = ((weight / variationMax.weight) * 100).toInt()
                            Text(
                                text = stringResource(
                                    Res.string.weight_selector_chin_subtitle,
                                    percentage,
                                    variation.fullName,
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}