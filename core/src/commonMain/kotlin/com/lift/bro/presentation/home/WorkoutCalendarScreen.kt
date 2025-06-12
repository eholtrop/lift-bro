package com.lift.bro.presentation.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Excercise
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.excercise.SetInfoRow
import com.lift.bro.ui.Calendar
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toString
import kotlinx.datetime.LocalDate


@Composable
fun WorkoutCalendarScreen(
    modifier: Modifier = Modifier,
    variationClicked: (Variation, LocalDate) -> Unit,
    excercises: List<Excercise>,
) {
    var selectedDate by remember { mutableStateOf(today) }

    val setDateMap = excercises.groupBy { it.date }

    val selectedDateSets: List<Excercise> = setDateMap[selectedDate] ?: emptyList()

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
                dateDecorations = { date ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                    ) {
                        setDateMap[date]?.map {
                            it.variation.lift?.color?.toColor() ?: MaterialTheme.colorScheme.primary
                        }?.take(4)?.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier.background(
                                    color = color,
                                    shape = CircleShape,
                                ).size(4.dp)
                            )
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
            selectedDateSets
        ) { excercise ->
            Card(
                modifier = Modifier.animateItem(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                val variation = excercise.variation

                Row(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.one,
                        vertical = MaterialTheme.spacing.half
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                            .clickable(
                                role = Role.Button,
                                onClick = { variationClicked(variation, selectedDate) }
                            )
                    ) {
                        Text(
                            text = "${variation.name} ${variation.lift?.name}",
                            style = MaterialTheme.typography.titleMedium,
                        )

                        excercise.sets.sortedByDescending { it.weight }
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
                            color = variation.lift?.color?.toColor()
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