package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.components.Calendar
import com.lift.bro.presentation.components.today
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.presentation.variation.formattedWeight
import com.lift.bro.presentation.variation.render

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    setClicked: (LBSet) -> Unit,
) {
    var selectedDate by remember { mutableStateOf(today) }

    val sets = dependencies.database.setDataSource.getAll()

    val selectedVariations = sets.filter { it.date.toLocalDate() == selectedDate }
        .groupBy { it.variationId }
        .toList()
        .sortedByDescending { it.second.maxOf { it.weight } }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
    ) {

        item {
            Calendar(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight(),
                selectedDate = selectedDate,
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
                dateSelected = {
                    selectedDate = it
                },
                date = { date ->
                },
                numberOfDotsForDate = { date ->
                    sets.filter { it.date.toLocalDate() == date }
                        .groupBy { it.variationId }.size
                }
            )
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(height = 2.dp)
                    .background(MaterialTheme.colorScheme.surfaceDim)
            ) {}
        }

        item {
            Text(
                text = selectedDate.toString("EEEE, MMM d - yyyy"),
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(
            selectedVariations
        ) { pair ->
            Card(
                modifier = Modifier.animateItem(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.half)
                ) {
                    val variation =
                        dependencies.database.variantDataSource.get(pair.first)
                    val lift by dependencies.database.liftDataSource.get(
                        variation?.lift?.id
                    )
                        .collectAsState(null)

                    Text(
                        text = "${variation?.name} ${lift?.name}",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    pair.second.sortedByDescending { it.weight }
                        .forEach { set ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .defaultMinSize(minHeight = 44.dp)
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { setClicked(set) }
                                    ),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${set.formattedWeight} x ${set.reps}",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                set.tempo.render()
                            }
                        }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}