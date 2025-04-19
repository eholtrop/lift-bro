package com.lift.bro.presentation.lift

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.toString
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarButton
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.utils.formattedWeight
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import kotlinx.datetime.LocalDate

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LiftDetailsScreen(
    liftId: String,
    editLiftClicked: () -> Unit,
    variationClicked: (String) -> Unit,
    addSetClicked: () -> Unit,
    onSetClicked: (LBSet) -> Unit,
    database: LBDatabase = dependencies.database,
) {
    val lift by database.liftDataSource.get(liftId).collectAsState(null)

    val variations by database.variantDataSource.listenAll(liftId).collectAsState(emptyList())

    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        BasicAlertDialog(
            onDismissRequest = {
                showColorPicker = false
            }
        ) {
            val controller = rememberColorPickerController()

            val defaultColor = MaterialTheme.colorScheme.primary

            var color by remember { mutableStateOf(lift?.color?.toColor() ?: defaultColor) }

            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large,
                ).padding(MaterialTheme.spacing.one),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Select a Color",
                    style = MaterialTheme.typography.titleLarge
                )

                Space(MaterialTheme.spacing.one)

                HsvColorPicker(
                    modifier = Modifier.size(
                        256.dp,
                        256.dp
                    ),
                    controller = controller,
                    onColorChanged = {
                        color = it.color
                    },
                    initialColor = color
                )

                Space(MaterialTheme.spacing.two)

                Box(
                    modifier = Modifier.background(
                        color = color,
                        shape = CircleShape,
                    )
                        .border(
                            1.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        )
                        .size(32.dp),
                    content = {}
                )

                Space(MaterialTheme.spacing.two)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = (color.red * 255).toInt().toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let {
                                color = color.copy(red = it / 255f)
                            }
                        },
                        label = {
                            Text("Red")
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                        ),
                    )

                    TextField(
                        modifier = Modifier.weight(1f),
                        value = (color.green * 255).toInt().toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let {
                                color = color.copy(green = it / 255f)
                            }
                        },
                        label = {
                            Text("Green")
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                        ),
                    )

                    TextField(
                        modifier = Modifier.weight(1f),
                        value = (color.blue * 255).toInt().toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let {
                                color = color.copy(blue = it / 255f)
                            }
                        },
                        label = {
                            Text("Blue")
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                        ),
                    )
                }

                Space(MaterialTheme.spacing.one)


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = {
                            showColorPicker = false
                        },
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Cancel")
                    }

                    Space(MaterialTheme.spacing.half)

                    Button(
                        onClick = {
                            lift?.let {
                                dependencies.database.liftDataSource.save(
                                    it.copy(
                                        color = color.value
                                    )
                                )
                            }
                            showColorPicker = false
                        },
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    lift?.let { lift ->
        LiftingScaffold(
            fabProperties = FabProperties(
                fabIcon = Icons.Default.Add,
                contentDescription = "Add Set",
                fabClicked = addSetClicked,
            ),
            title = lift.name,
            trailingContent = {
                TopBarButton(
                    onClick = {
                        showColorPicker = true
                    }
                ) {
                    Box(
                        modifier = Modifier.background(
                            color = lift.color?.toColor() ?: MaterialTheme.colorScheme.primary,
                        ).fillMaxSize()
                    ) { }
                }
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
private fun VariationCard(
    variation: Variation,
    parentLift: Lift,
    onClick: (Variation) -> Unit,
    onSetClicked: (LBSet) -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.surface,
            )
            .padding(MaterialTheme.spacing.quarter),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(
                    vertical = MaterialTheme.spacing.one,
                    horizontal = MaterialTheme.spacing.half
                ),
        ) {

            val sets by dependencies.database.setDataSource.listenAllForVariation(variation.id).collectAsState(emptyList())

            Row(
                modifier = Modifier.clickable {
                    onClick(variation)
                }.padding(horizontal = MaterialTheme.spacing.half),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
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

                    Text(
                        text = maxLift?.let { "${it.formattedWeight} Max" } ?: run { "No Max" },
                        style = MaterialTheme.typography.titleSmall
                    )

                }

                Space(MaterialTheme.spacing.one)

                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
            }

            Space(MaterialTheme.spacing.one)

            val setPoints = sets.groupBy { it.date.toLocalDate() }.toList()
                .sortedByDescending { it.first }


            if (setPoints.isNotEmpty()) {
                var selectedData: DotGraphData? by remember {
                    mutableStateOf(setPoints.maxBy { it.first }.let {
                        DotGraphData(
                            it.first.toEpochDays().toLong(),
                            it.second.maxOf { it.weight.toFloat() }
                        )
                    })
                }

                DotGraph(
                    modifier = Modifier.fillMaxWidth().height(128.dp),
                    data = setPoints.map {
                        DotGraphData(
                            it.first.toEpochDays().toLong(),
                            it.second.maxOf { it.weight.toFloat() }
                        )
                    },
                    state = rememberLazyListState(),
                    selectedData = selectedData,
                    xAxis = { epochDays ->
                        Text(
                            LocalDate.fromEpochDays(epochDays.toInt()).toString("MMM d"),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selectedData?.x == epochDays) parentLift.color?.toColor() ?: MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    yAxis = { fl: Float, fl1: Float -> },
                    dataPointClicked = {
                        selectedData = it
                    },
                    colors = DotGraphColors(
                        dotColor = MaterialTheme.colorScheme.onSurface,
                        dotColorSelected = parentLift.color?.toColor() ?: MaterialTheme.colorScheme.primary
                    )
                )

                selectedData?.let { data ->

                    Space(MaterialTheme.spacing.one)

                    val pair = setPoints.first { it.first.toEpochDays().toLong() == data.x }
                    Column(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.half)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Start),
                            text = pair.first.toString(pattern = "EEEE MMM, d"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(
                            modifier = Modifier.height(1.dp).fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                        )
                    }

                    if (pair.second.isNotEmpty()) {
                        Space(MaterialTheme.spacing.half)
                    }

                    pair.second
                        .sortedByDescending { it.weight }
                        .forEach { set ->
                            Column(
                                modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { onSetClicked(set) }
                                    )
                                    .padding(horizontal = MaterialTheme.spacing.half),
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
            } else {

            }

        }
    }
}