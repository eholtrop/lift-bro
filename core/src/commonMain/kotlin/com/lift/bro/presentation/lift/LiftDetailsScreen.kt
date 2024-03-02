package com.lift.bro.presentation.lift

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.Color
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.components.Calendar
import com.lift.bro.presentation.components.today
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.variation.formattedMax
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
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

            var selectedDate by remember { mutableStateOf(today) }

            val sets = variations.map {
                database.setDataSource.getAll(it.id)
            }
                .fold(emptyList<LBSet>()) { l1, l2 -> l1 + l2 }

            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {

                item {
                    Calendar(
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentHeight(),
                        selectedDate = selectedDate,
                        contentPadding = PaddingValues(MaterialTheme.spacing.one),
                        dateSelected = {
                            selectedDate = it
                        },
                        date = { date ->
                            val selected = date == selectedDate
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else if (sets.any { it.date.toLocalDate() == date }) {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedDate = date
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    )
                }

                items(variations.filter { v ->
                    sets
                        .filter { it.date.toLocalDate() == selectedDate }
                        .any { it.variationId == v.id }
                }) { variation ->
                    Text(
                        text = selectedDate.dayOfWeek.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    VariationCard(
                        variation = variation,
                        onClick = { variationClicked(variation.id) }
                    )
                }

                items(variations.filter { v ->
                    !sets
                        .filter { it.date.toLocalDate() == selectedDate }
                        .any { it.variationId == v.id }
                }) { variation ->
                    Text(
                        text = "Other Variations",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    VariationCard(
                        variation = variation,
                        onClick = { variationClicked(variation.id) }
                    )
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
        modifier = Modifier.fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.one
            ),
        onClick = { onClick(variation) }
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(variation.name ?: "")

            val maxLift = dependencies.database.setDataSource.getAll(variation.id)
                .fold(null as LBSet?) { maxLift, currentSet ->
                    when {
                        maxLift == null || maxLift.weight < currentSet.weight -> currentSet
                        else -> maxLift
                    }
                }

            maxLift?.let { lift ->
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.quarter))
                Text(lift.formattedMax)
            } ?: run {
                Text("No Max")
            }
        }
    }
}