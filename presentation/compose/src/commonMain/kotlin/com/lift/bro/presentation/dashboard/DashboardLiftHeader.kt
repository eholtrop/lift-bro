package com.lift.bro.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.set.ChipButton
import com.lift.bro.ui.Space
import com.lift.bro.ui.card.lift.LiftCardYValue
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.reps
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.padding.vertical.padding

@Composable
fun DashboardLiftHeader(
    modifier: Modifier = Modifier,
    v2: Boolean,
    sortingSettings: SortingSettings,
    showWeight: MutableState<LiftCardYValue> = LocalLiftCardYValue.current,
    showTempo: Boolean,
    showRpe: Boolean,
    onToggleTempo: () -> Unit,
    onToggleRpe: () -> Unit,
    optionSelected: (SortingOption) -> Unit,
    toggleFavourite: () -> Unit,
    onAddCategoryClicked: () -> Unit,
) {
    val thisModifier = if (v2) {
        modifier.padding(
            top = MaterialTheme.spacing.one,
            bottom = MaterialTheme.spacing.half
        )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        Color.Transparent,
                    ),
                    end = Offset(80f, 50f),
                ),
                shape = MaterialTheme.shapes.medium.copy(
                    bottomEnd = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp),
                ),
            ).padding(
                top = MaterialTheme.spacing.threeQuarters,
                horizontal = MaterialTheme.spacing.half
            )
    } else {
        modifier.padding(
            horizontal = MaterialTheme.spacing.half,
            top = MaterialTheme.spacing.two
        )
    }

    Column(
        modifier = thisModifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = {
                    onAddCategoryClicked()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Category"
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
        ) {
            ChipButton(
                onClick = {
                    showWeight.value =
                        if (showWeight.value == LiftCardYValue.Weight) {
                            LiftCardYValue.Reps
                        } else {
                            LiftCardYValue.Weight
                        }
                }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = LocalTextStyle.current
                                .copy(
                                    color = if (showWeight.value == LiftCardYValue.Weight) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        TextFieldDefaults.colors().disabledTextColor
                                    }
                                )
                                .toSpanStyle()
                        ) {
                            append(LocalUnitOfMeasure.current.value)
                        }
                        append("/")
                        withStyle(
                            style = LocalTextStyle.current
                                .copy(
                                    color = if (showWeight.value == LiftCardYValue.Reps) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        TextFieldDefaults.colors().disabledTextColor
                                    }
                                )
                                .toSpanStyle()
                        ) {
                            append(stringResource(Res.string.reps))
                        }
                    }
                )
            }
            ChipButton(
                onClick = {
                    onToggleRpe()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null
                )
                Text(
                    text = "rpe",
                    color = if (showRpe) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        TextFieldDefaults.colors().disabledTextColor
                    }
                )
            }
            ChipButton(
                onClick = {
                    onToggleTempo()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null
                )
                Text(
                    text = "tempo",
                    color = if (showTempo) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        TextFieldDefaults.colors().disabledTextColor
                    }
                )
            }

            Space()

            var showSortingDialog by remember { mutableStateOf(false) }

            if (showSortingDialog) {
                DashboardSortingDialog(
                    sortingSettings = sortingSettings,
                    toggleFavourite = toggleFavourite,
                    optionSelected = optionSelected,
                    onDismissRequest = { showSortingDialog = false },
                )
            }

            ChipButton(
                onClick = {
                    showSortingDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Sort,
                    contentDescription = "Sort"
                )
            }
        }
    }
}

@Preview
@Composable
fun DashboardLiftHeaderV2Preview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column {
            // All permutations for v2=true
            // showWeight: Weight, showTempo: false, showRpe: false
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = false,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Weight, showTempo: true, showRpe: false
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = true,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Weight, showTempo: false, showRpe: true
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = false,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Weight, showTempo: true, showRpe: true
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = true,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: false, showRpe: false
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = false,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: true, showRpe: false
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = true,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: false, showRpe: true
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = false,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: true, showRpe: true
            DashboardLiftHeader(
                v2 = true,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = true,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
        }
    }
}

@Preview
@Composable
fun DashboardLiftHeaderV1Preview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column {
            // All permutations for v2=false
            // showWeight: Weight, showTempo: false, showRpe: false
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = false,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Weight, showTempo: true, showRpe: false
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = true,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Weight, showTempo: false, showRpe: true
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = false,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Weight, showTempo: true, showRpe: true
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Weight),
                showTempo = true,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: false, showRpe: false
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = false,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: true, showRpe: false
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = true,
                showRpe = false,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: false, showRpe: true
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = false,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
            // showWeight: Reps, showTempo: true, showRpe: true
            DashboardLiftHeader(
                v2 = false,
                sortingSettings = SortingSettings(),
                showWeight = mutableStateOf(LiftCardYValue.Reps),
                showTempo = true,
                showRpe = true,
                onToggleTempo = {},
                onToggleRpe = {},
                optionSelected = {},
                toggleFavourite = {},
                onAddCategoryClicked = {}
            )
        }
    }
}
