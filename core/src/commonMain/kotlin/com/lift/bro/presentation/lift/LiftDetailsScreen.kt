@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.lift

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.presentation.variation.formattedWeight
import com.lift.bro.presentation.variation.render
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
    onSetClicked: (LBSet) -> Unit,
    database: LBDatabase = dependencies.database,
) {
    val lift by database.liftDataSource.get(liftId).collectAsState(null)

    val variations by database.variantDataSource.listenAll(liftId).collectAsState(emptyList())


    lift?.let { lift ->
        LiftingScaffold(
            fabIcon = Icons.Default.Add,
            contentDescription = "Add Set",
            fabClicked = addSetClicked,
            title = lift.name,
            actions = {
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
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                items(variations) { variation ->
                    VariationCard(
                        variation = variation,
                        parentLift = lift,
                        onClick = { variationClicked(variation.id) },
                        onSetClicked = onSetClicked
                    )
                }
            }
        }
    }
}

@Composable
fun VariationCard(
    variation: Variation,
    parentLift: Lift,
    onClick: (Variation) -> Unit,
    onSetClicked: (LBSet) -> Unit,
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
                    modifier = Modifier.weight(1f),
                    text = "${variation.name} ${parentLift.name}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                val maxLift = sets
                    .fold(null as LBSet?) { maxLift, currentSet ->
                        when {
                            maxLift == null || maxLift.weight < currentSet.weight -> currentSet
                            else -> maxLift
                        }
                    }

                Space(MaterialTheme.spacing.one)

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

            Space(MaterialTheme.spacing.half)

            sets.groupBy { it.date.toLocalDate() }.toList()
                .sortedByDescending { it.first }
                .take(3)
                .forEach { pair ->


                    Text(
                        modifier = Modifier.align(Alignment.Start),
                        text = pair.first.toString(pattern = "EEEE MMM, d"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier.height(1.dp).fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                    )

                    Space(MaterialTheme.spacing.half)

                    pair.second
                        .sortedByDescending { it.weight }
                        .forEach { set ->
                            Column(
                                modifier = Modifier
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { onSetClicked(set) }
                                    )
                                    .defaultMinSize(minHeight = 44.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${set.formattedWeight} x ${set.reps}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                set.tempo.render()
                            }
                        }
                }

            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = { onClick(variation) }
            ) {
                Text("See All")
            }
        }
    }
}