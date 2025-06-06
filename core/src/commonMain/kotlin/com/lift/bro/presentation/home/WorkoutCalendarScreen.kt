package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.excercise.SetInfoRow
import com.lift.bro.ui.Calendar
import com.lift.bro.ui.CalendarDateStyle
import com.lift.bro.ui.today
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.toString
import com.lift.bro.ui.Space
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate


@Composable
fun WorkoutCalendarScreen(
    modifier: Modifier = Modifier,
    variationClicked: (Variation, LocalDate) -> Unit,
) {
    var selectedDate by remember { mutableStateOf(today) }

    val setDateMap by dependencies.database.setDataSource.listenAll()
        .map { it.groupBy { it.date.toLocalDate() } }
        .collectAsState(emptyMap())

    val selectedDateSets: List<LBSet> = setDateMap[selectedDate] ?: emptyList()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
    ) {

        item {
            val defaultColor = MaterialTheme.colorScheme.primary

            val variations by dependencies.database.variantDataSource.listenAll().collectAsState(
                emptyList()
            )

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
                dateDecorations = { date ->
                    var dots by remember { mutableStateOf(emptyList<Color>()) }

                    LaunchedEffect(setDateMap, variations) {
                        val sets = setDateMap[date] ?: emptyList()

                        if (sets.isNotEmpty()) {
                            dots = variations.filter { variation ->
                                sets.any { it.variationId == variation.id }
                            }.map { it.lift?.color?.toColor() ?: defaultColor }
                        }
                    }

                    AnimatedVisibility(dots.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                        ) {
                            dots.forEach {
                                Box(
                                    modifier = Modifier.background(
                                        color = if (selectedDate == date) MaterialTheme.colorScheme.onSecondary else it,
                                        shape = CircleShape,
                                    ).size(4.dp)
                                )
                            }
                        }
                    }
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
            selectedDateSets.groupBy { it.variationId }.toList()
        ) { pair ->
            Card(
                modifier = Modifier.animateItem(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.one,
                        vertical = MaterialTheme.spacing.half
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val variation = dependencies.database.variantDataSource.get(pair.first)
                    Column(
                        modifier = Modifier.weight(1f)
                            .clickable(
                                role = Role.Button,
                                onClick = { variationClicked(variation!!, selectedDate) }
                            )
                    ) {
                        Text(
                            text = "${variation?.name} ${variation?.lift?.name}",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        pair.second.sortedByDescending { it.weight }
                            .forEach { set ->
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                        .defaultMinSize(minHeight = 44.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    SetInfoRow(set = set)
                                }
                            }
                    }

                    Space(MaterialTheme.spacing.half)

                    Box(
                        modifier = Modifier.background(
                            color = variation?.lift?.color?.toColor()
                                ?: MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ).height(MaterialTheme.spacing.oneAndHalf).aspectRatio(1f),
                        content = {}
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}