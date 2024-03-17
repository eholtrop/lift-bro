package com.lift.bro.presentation.lift

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.presentation.variation.formattedWeight
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Instant.toLocalDate() = this.toLocalDateTime(TimeZone.currentSystemDefault()).date

@Composable
fun LiftDetailsScreen(
    liftId: String,
    editLiftClicked: () -> Unit,
    addVariationClicked: () -> Unit,
    variationClicked: (String) -> Unit,
    addSetClicked: () -> Unit,
    database: LBDatabase = dependencies.database,
) {
    val lift by database.liftDataSource.get(liftId).collectAsState(null)

    val variations by database.variantDataSource.listenAll(liftId).collectAsState(emptyList())


    lift?.let { lift ->
        LiftingScaffold(
            fabText = "Add Set",
            fabClicked = addSetClicked,
            topBar = {
                TopBar(
                    title = lift.name,
                    showBackButton = true,
                    trailingContent = {
                        TopBarIconButton(
                            Icons.Default.Add,
                            contentDescription = "Add Variation",
                            onClick = addVariationClicked,
                        )
                        TopBarIconButton(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            onClick = editLiftClicked,
                        )
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
            ) {
                items(variations) { variation ->
                    VariationCard(
                        variation = variation,
                        onClick = { variationClicked(variation.id) }
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
                }
            }
        }
    }
}

@Composable
fun VariationCard(
    variation: Variation,
    onClick: (Variation) -> Unit,
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(variation) },
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one)
                .fillMaxWidth(),
        ) {

            val sets = dependencies.database.setDataSource.getAll(variation.id)

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = variation.name ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Space()
                val maxLift = sets
                    .fold(null as LBSet?) { maxLift, currentSet ->
                        when {
                            maxLift == null || maxLift.weight < currentSet.weight -> currentSet
                            else -> maxLift
                        }
                    }

                maxLift?.let { lift ->
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = lift.formattedWeight,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Max",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } ?: run {
                    Text(
                        "No Max",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            sets.groupBy { it.date.toLocalDate() }.toList().sortedByDescending { it.first }
                .take(3).forEach { pair ->
                    Text(
                        text = pair.first.toString(pattern = "EEEE MMM, d"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    pair.second.sortedByDescending { it.weight }.forEach { set ->
                        Text(
                            text = "${set.formattedWeight} x ${set.reps}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
        }
    }
}