@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.category

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.estimateMax
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.oneRepMax
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.dashboard.DashboardLiftHeader
import com.lift.bro.presentation.dashboard.SortingSettings
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarButton
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.card.lift.LiftCardYValue
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.transparentColors
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.maxText
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.lift_details_fab_content_description
import lift_bro.core.generated.resources.lift_details_screen_favourite_content_description
import lift_bro.core.generated.resources.variation_details_screen_edit_title_content_description
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.AccessibilityMinimumSize
import tv.dpal.compose.listCorners
import tv.dpal.compose.toColor
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.ktx.datetime.toLocalDate

@Composable
fun CategoryDetailsScreen(
    liftId: String,
) {
    CategoryDetailsScreen(
        interactor = rememberCategoryDetailsInteractor(categoryId = liftId)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryDetailsScreen(
    interactor: Interactor<CategoryDetailsState, CategoryDetailsEvent>,
) {
    val state by interactor.state.collectAsState()

    var showColorPicker by remember { mutableStateOf(false) }
    var showDeleteWarning by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            color = state.categoryColor?.toColor() ?: MaterialTheme.colorScheme.primary,
            onDismissRequest = { showColorPicker = false },
            onColorSelected = {
                interactor(CategoryDetailsEvent.CategoryColorChanged(it.value))
                showColorPicker = false
            }
        )
    }

    if (showDeleteWarning) {
        WarningDialog(
            title = "Are you sure?",
            text = "This will delete the Category, All movements will stay!",
            onConfirm = {
                interactor(CategoryDetailsEvent.DeleteCategoryClicked)
                showDeleteWarning = false
            },
            onDismiss = { showDeleteWarning = false }
        )
    }

    LiftingScaffold(
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Add,
            contentDescription = stringResource(Res.string.lift_details_fab_content_description),
            fabClicked = {
                interactor(CategoryDetailsEvent.AddSetClicked)
            },
        ),
        title = {
            var editName by remember { mutableStateOf(state.categoryName != null) }
            var name by remember(state.categoryName) { mutableStateOf(state.categoryName ?: "") }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (editName) {
                    TextField(
                        modifier = Modifier.wrapContentWidth(),
                        value = name,
                        textStyle = MaterialTheme.typography.headlineMedium,
                        onValueChange = {
                            name = it
                        },
                        colors = TextFieldDefaults.transparentColors(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Category Name",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        supportingText = {
                            Text("Squats, Leg Day, Back Stuff...")
                        },
                        suffix = {
                            IconButton(
                                onClick = {
                                    interactor(CategoryDetailsEvent.NameUpdated(name))
                                    editName = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        keyboardActions = KeyboardActions(
                            onAny = {
                                interactor(CategoryDetailsEvent.NameUpdated(name))
                                editName = false
                            }
                        )
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(
                                onClick = {
                                    editName = true
                                },
                                role = Role.Button
                            )
                            .padding(
                                horizontal = MaterialTheme.spacing.one
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
                    ) {
                        Text(state.categoryName ?: "Category Name")
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(
                                Res.string.variation_details_screen_edit_title_content_description
                            ),
                        )
                    }
                }
            }
        },
        trailingContent = {
            if (state.categoryName != null) {
                TopBarButton(
                    onClick = {
                        showColorPicker = true
                    }
                ) {
                    Box(
                        modifier = Modifier.background(
                            color = state.categoryColor?.toColor() ?: MaterialTheme.colorScheme.primary,
                        ).fillMaxSize()
                    ) { }
                }
                TopBarIconButton(
                    onClick = {
                        showDeleteWarning = true
                    },
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Category,"
                )
            }
        }
    ) { padding ->

        var sorting by remember { mutableStateOf(SortingOptions.Name) }
        var showRpe by remember { mutableStateOf(false) }

        if (state.categoryName != null) {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                item {
                    DashboardLiftHeader(
                        v2 = false,
                        showRpe = showRpe,
                        title = "Movements",
                        onToggleTempo = { },
                        onToggleRpe = { showRpe = !showRpe },
                        optionSelected = { },
                        toggleFavourite = { },
                        onAddClicked = {
                            interactor(CategoryDetailsEvent.CreateMovementClicked)
                        },
                        sortingSettings = SortingSettings(),
                        showTempo = null,
                    )
                }

                items(
                    state.movements.let {
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
                        onClick = { interactor(CategoryDetailsEvent.MovementClicked(it)) },
                        onSetClicked = {
                            interactor(CategoryDetailsEvent.SetClicked(it))
                        },
                        showRpe = showRpe,
                        favouriteToggled = {
                            interactor(CategoryDetailsEvent.ToggleFavourite(it))
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

@Composable
private fun VariationCard(
    modifier: Modifier = Modifier,
    state: MovementCardState,
    showRpe: Boolean,
    onClick: (Movement) -> Unit,
    onSetClicked: (LBSet) -> Unit,
    favouriteToggled: (Movement) -> Unit,
) {
    val variation = state.variation
    val sets = state.sets
    Box(
        modifier = modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        Color.Transparent,
                    )
                )
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
                                contentDescription = stringResource(
                                    Res.string.lift_details_screen_favourite_content_description
                                )
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
                            lineGraphData = if (showRpe) topLift.rpe?.toFloat()?.div(10f) ?: 0f else 0f
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
                        ?.forEachIndexed { index, set ->
                            val rowShape = MaterialTheme.shapes.small.listCorners(index, pair.second)
                            SetInfoRow(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        horizontal = MaterialTheme.spacing.half
                                    )
                                    .clip(rowShape)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        shape = rowShape,
                                    )
                                    .border(
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                                        shape = rowShape,
                                    )
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { onSetClicked(set) }
                                    ).padding(
                                        all = MaterialTheme.spacing.half,
                                    ),
                                set = set,
                            )
                        }

                    if (pair?.second?.isNotEmpty() == true) {
                        Space(MaterialTheme.spacing.half)
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

class LiftDetailsStateProvider: PreviewParameterProvider<CategoryDetailsState> {
    override val values: Sequence<CategoryDetailsState>
        get() = sequenceOf(
            // Empty lift - no variations
            CategoryDetailsState(
                categoryId = "",
                categoryName = "Squat",
                categoryColor = 0xFF2196F3uL,
                movements = emptyList()
            ),
            // Lift with one variation, no sets
            CategoryDetailsState(
                categoryId = "",
                categoryName = "Bench Press",
                categoryColor = 0xFF4CAF50uL,
                movements = listOf(
                    MovementCardState(
                        variation = Movement(
                            lift = com.lift.bro.domain.models.Category(
                                name = "Bench Press",
                                color = 0xFF4CAF50uL
                            ),
                            name = "Flat Bench",
                            favourite = true
                        ),
                        sets = emptyList()
                    )
                )
            ),
            // Lift with multiple variations and sets
            CategoryDetailsState(
                categoryId = "",
                categoryName = "Deadlift",
                categoryColor = 0xFFFF5722uL,
                movements = listOf(
                    MovementCardState(
                        variation = Movement(
                            lift = com.lift.bro.domain.models.Category(
                                name = "Deadlift",
                                color = 0xFFFF5722uL
                            ),
                            name = "Conventional",
                            favourite = true
                        ),
                        sets = listOf(
                            LBSet(
                                id = "set1",
                                variationId = "var1",
                                weight = 405.0,
                                reps = 5,
                                rpe = 8,
                                date = kotlin.time.Clock.System.now(),
                            ),
                            LBSet(
                                id = "set2",
                                variationId = "var1",
                                weight = 425.0,
                                reps = 3,
                                rpe = 9,
                                date = kotlin.time.Clock.System.now(),
                            )
                        )
                    ),
                    MovementCardState(
                        variation = Movement(
                            lift = com.lift.bro.domain.models.Category(
                                name = "Deadlift",
                                color = 0xFFFF5722uL
                            ),
                            name = "Sumo",
                            favourite = false
                        ),
                        sets = listOf(
                            LBSet(
                                id = "set3",
                                variationId = "var2",
                                weight = 365.0,
                                reps = 5,
                                rpe = 7,
                                date = kotlin.time.Clock.System.now()
                            )
                        )
                    )
                )
            )
        )
}
