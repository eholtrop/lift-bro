package com.lift.bro.presentation.lift

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.estimateMax
import com.lift.bro.domain.models.oneRepMax
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftCardYValue
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarButton
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.fullName
import com.lift.bro.utils.maxText
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.color_picker_dialog_blue
import lift_bro.core.generated.resources.color_picker_dialog_green
import lift_bro.core.generated.resources.color_picker_dialog_red
import lift_bro.core.generated.resources.color_picker_dialog_title
import lift_bro.core.generated.resources.color_picker_negative_cta
import lift_bro.core.generated.resources.color_picker_positive_cta
import lift_bro.core.generated.resources.lift_details_fab_content_description
import lift_bro.core.generated.resources.reps
import org.jetbrains.compose.resources.stringResource

@Composable
fun LiftDetailsScreen(
    liftId: String
) {
    LiftDetailsScreen(
        interactor = rememberLiftDetailsInteractor(liftId = liftId)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LiftDetailsScreen(
    interactor: Interactor<LiftDetailsState, LiftDetailsEvent>
) {
    val state by interactor.state.collectAsState()

    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        BasicAlertDialog(
            onDismissRequest = {
                showColorPicker = false
            }
        ) {
            val controller = rememberColorPickerController()

            val defaultColor = MaterialTheme.colorScheme.primary

            var color by remember { mutableStateOf(state.liftColor?.toColor() ?: defaultColor) }

            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large,
                ).padding(MaterialTheme.spacing.one),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.color_picker_dialog_title),
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
                            Text(stringResource(Res.string.color_picker_dialog_red))
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
                            Text(stringResource(Res.string.color_picker_dialog_green))
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
                            Text(stringResource(Res.string.color_picker_dialog_blue))
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
                        Text(stringResource(Res.string.color_picker_negative_cta))
                    }

                    Space(MaterialTheme.spacing.half)

                    Button(
                        onClick = {
                            interactor(LiftDetailsEvent.LiftColorChanged(color.value))
                            showColorPicker = false
                        },
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text(stringResource(Res.string.color_picker_positive_cta))
                    }
                }
            }
        }
    }

    LiftingScaffold(
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Add,
            contentDescription = stringResource(Res.string.lift_details_fab_content_description),
            fabClicked = {
                interactor(LiftDetailsEvent.AddSetClicked)
            },
        ),
        title = { Text(state.liftName ?: "") },
        trailingContent = {
            TopBarButton(
                onClick = {
                    showColorPicker = true
                }
            ) {
                Box(
                    modifier = Modifier.background(
                        color = state.liftColor?.toColor() ?: MaterialTheme.colorScheme.primary,
                    ).fillMaxSize()
                ) { }
            }
            TopBarIconButton(
                Icons.Default.Edit,
                contentDescription = "Edit",
                onClick = {
                    interactor(LiftDetailsEvent.EditLiftClicked)
                },
            )
        }
    ) { padding ->

        var sorting by remember { mutableStateOf(SortingOptions.Name) }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = {
                            try {
                                sorting = SortingOptions.values()[sorting.ordinal + 1]
                            } catch (e: Exception) {
                                sorting = SortingOptions.values()[0]
                            }
                        }
                    ) {
                        Icon(
                            modifier = when (sorting) {
                                SortingOptions.Name -> Modifier
                                SortingOptions.NameReversed -> Modifier.graphicsLayer {
                                    scaleY = -1f
                                }

                                SortingOptions.MaxSet -> Modifier
                                SortingOptions.MaxSetReversed -> Modifier.graphicsLayer {
                                    scaleY = -1f
                                }
                            },
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort By"
                        )
                        Space(MaterialTheme.spacing.half)
                        Text(
                            text = when (sorting) {
                                SortingOptions.Name -> "Name"
                                SortingOptions.NameReversed -> "Name"
                                SortingOptions.MaxSet -> "One Rep Max"
                                SortingOptions.MaxSetReversed -> "One Rep Max"
                            }
                        )
                    }

                    Space(MaterialTheme.spacing.half)

                    val yValue = LocalLiftCardYValue.current
                    Button(
                        onClick = {
                            yValue.value =
                                if (yValue.value == LiftCardYValue.Weight) LiftCardYValue.Reps else LiftCardYValue.Weight
                        }
                    ) {
                        Text(
                            text = if (yValue.value == LiftCardYValue.Weight) {
                                LocalUnitOfMeasure.current.value
                            } else {
                                stringResource(
                                    Res.string.reps
                                )
                            }
                        )
                    }
                }
            }

            items(
                state.variations.let {
                    when (sorting) {
                        SortingOptions.Name -> it.sortedBy { it.variation.name ?: "" }
                        SortingOptions.NameReversed -> it.sortedByDescending { it.variation.name ?: "" }
                        SortingOptions.MaxSet -> it.sortedByDescending {
                            it.variation.oneRepMax?.oneRepMax ?: it.variation.eMax?.estimateMax ?: 0.0
                        }

                        SortingOptions.MaxSetReversed -> it.sortedBy {
                            it.variation.oneRepMax?.oneRepMax ?: it.variation.eMax?.estimateMax ?: 0.0
                        }
                    }.sortedByDescending { it.variation.favourite }
                },
                key = { it.variation.id },
            ) { cardState ->
                VariationCard(
                    modifier = Modifier.animateItem(),
                    state = cardState,
                    onClick = { interactor(LiftDetailsEvent.VariationClicked(it)) },
                    onSetClicked = {
                        interactor(LiftDetailsEvent.SetClicked(it))
                    },
                    favouriteToggled = {
                        interactor(LiftDetailsEvent.ToggleFavourite(it))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun VariationCard(
    modifier: Modifier = Modifier,
    state: VariationCardState,
    onClick: (Variation) -> Unit,
    onSetClicked: (LBSet) -> Unit,
    favouriteToggled: (Variation) -> Unit
) {
    val variation = state.variation
    val sets = state.sets
    Box(
        modifier = modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.clickable {
                    onClick(variation)
                }.padding(
                    start = MaterialTheme.spacing.one,
                    end = MaterialTheme.spacing.one,
                    top = MaterialTheme.spacing.one
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = variation.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Space(MaterialTheme.spacing.quarter)
                        IconButton(
                            modifier = Modifier.size(Dp.AccessibilityMinimumSize),
                            onClick = {
                                favouriteToggled(variation)
                            }
                        ) {
                            Icon(
                                imageVector = if (variation.favourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite"
                            )
                        }
                    }

                    when (LocalLiftCardYValue.current.value) {
                        LiftCardYValue.Weight -> {
                            Text(
                                text = variation.maxText(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        LiftCardYValue.Reps -> {
                            Text(
                                text = sets.maxOfOrNull { it.reps }?.let { "$it Reps" }
                                    ?: run { "No Sets" },
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }

                    Space(MaterialTheme.spacing.one)
                }

                Space(MaterialTheme.spacing.one)

                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
            }

            val setPoints = sets.groupBy { it.date.toLocalDate() }.toList()
                .sortedByDescending { it.first }

            if (setPoints.isNotEmpty()) {
                var selectedData: LocalDate? by remember {
                    mutableStateOf(setPoints.maxOf { it.first })
                }

                DotGraph(
                    modifier = Modifier.fillMaxWidth().height(128.dp)
                        .padding(horizontal = MaterialTheme.spacing.quarter),
                    graphData = setPoints.map {
                        val topLift = it.second.maxBy {
                            when (LocalLiftCardYValue.current.value) {
                                LiftCardYValue.Weight -> it.weight.toFloat()
                                LiftCardYValue.Reps -> it.reps.toFloat()
                            }
                        }

                        it.first to GraphData(
                            date = it.first,
                            dotGraphData = when (LocalLiftCardYValue.current.value) {
                                LiftCardYValue.Weight -> topLift.weight.toFloat()
                                LiftCardYValue.Reps -> topLift.reps.toFloat()
                            },
                            lineGraphData = topLift.rpe?.toFloat()?.div(10f) ?: 0f
                        )
                    },
                    state = rememberLazyListState(),
                    selectedData = selectedData,
                    xAxis = { epochDays ->
                        Text(
                            LocalDate.fromEpochDays(epochDays.toInt()).toString("MMM d"),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selectedData?.toEpochDays()
                                    ?.toLong() == epochDays
                            ) {
                                variation.lift?.color?.toColor()
                                    ?: MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    yAxis = { fl: Float, fl1: Float -> },
                    dataPointClicked = {
                        selectedData = it
                    },
                    colors = DotGraphColors(
                        dotColor = MaterialTheme.colorScheme.onSurface,
                        dotColorSelected = variation.lift?.color?.toColor()
                            ?: MaterialTheme.colorScheme.primary
                    )
                )

                selectedData?.let { data ->

                    Space(MaterialTheme.spacing.one)

                    val pair = setPoints.firstOrNull { it.first == data }

                    Column(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(MaterialTheme.typography.titleMedium.toSpanStyle()) {
                                    append(pair?.first?.toString(pattern = "EEEE MMM, d"))
                                }
                                withStyle(MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                                    if (LocalTwmSettings.current) {
                                        append(" ")
                                        append(
                                            pair?.second?.sumOf { it.weight }.decimalFormat().uom()
                                        )
                                    }

                                    with(pair?.second?.sumOf { it.mer } ?: 0) {
                                        if (LocalShowMERCalcs.current?.enabled == true && this > 0) {
                                            append(" (+${this}mer)")
                                        }
                                    }
                                }
                            }
                        )

                        Box(
                            modifier = Modifier.height(1.dp).fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                        )
                    }

                    if (pair?.second?.isNotEmpty() == true) {
                        Space(MaterialTheme.spacing.half)
                    }

                    pair?.second
                        ?.sortedByDescending { it.weight }
                        ?.forEach { set ->
                            SetInfoRow(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { onSetClicked(set) }
                                    ).padding(
                                        horizontal = MaterialTheme.spacing.one,
                                        vertical = MaterialTheme.spacing.half,
                                    ),
                                set = set,
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun String.uom() = "$this ${LocalUnitOfMeasure.current.value}"

private enum class SortingOptions(reversed: Boolean) {
    Name(false),
    NameReversed(true),
    MaxSet(false),
    MaxSetReversed(true),
}
