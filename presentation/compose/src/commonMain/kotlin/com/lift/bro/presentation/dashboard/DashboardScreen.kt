@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.dashboard.DashboardEvent.LiftClicked
import com.lift.bro.ui.Card
import com.lift.bro.ui.card.lift.LiftCard
import com.lift.bro.ui.card.lift.LiftCardYValue
import com.lift.bro.ui.ReleaseNotesRow
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_footer_version
import lift_bro.core.generated.resources.reps
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    interactor: DashboardInteractor = rememberDashboardInteractor(),
) {
    val state by interactor.state.collectAsState()

    val showWeight = LocalLiftCardYValue.current

    Crossfade(
        modifier = modifier,
        targetState = state,
        label = "DashboardContent"
    ) { state ->
        when (state) {
            is Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Loaded -> {
                var showRpe by rememberSaveable { mutableStateOf(true) }
                var showTempo by rememberSaveable { mutableStateOf(true) }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(MaterialTheme.spacing.one),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                ) {
                    item(
                        span = { GridItemSpan(2) }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Row(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.medium
                                ).border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = MaterialTheme.shapes.medium
                                )
                                    .animateContentSize()
                            ) {
                                var showButtons by remember { mutableStateOf(false) }
                                if (!showButtons) {
                                    IconButton(
                                        onClick = {
                                            showButtons = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BarChart,
                                            contentDescription = "Graph Settings"
                                        )
                                    }
                                } else {
                                    Button(
                                        colors = ButtonDefaults.textButtonColors(),
                                        shape = MaterialTheme.shapes.medium.copy(
                                            topEnd = CornerSize(0.dp), bottomEnd = CornerSize(0.dp),
                                        ),
                                        onClick = {
                                            showWeight.value =
                                                if (showWeight.value == LiftCardYValue.Weight) {
                                                    LiftCardYValue.Reps
                                                } else {
                                                    LiftCardYValue.Weight
                                                }
                                        }
                                    ) {
dd
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    style = LocalTextStyle.current
                                                        .copy(color = if (showWeight.value == LiftCardYValue.Weight) MaterialTheme.colorScheme.primary else TextFieldDefaults.colors().disabledTextColor)
                                                        .toSpanStyle()
                                                ) {
                                                    append(LocalUnitOfMeasure.current.value)
                                                }
                                                append("/")
                                                withStyle(
                                                    style = LocalTextStyle.current
                                                        .copy(color = if (showWeight.value == LiftCardYValue.Reps) MaterialTheme.colorScheme.primary else TextFieldDefaults.colors().disabledTextColor)
                                                        .toSpanStyle()
                                                ) {
                                                    append(stringResource(Res.string.reps))
                                                }
                                            }
                                        )
                                    }
                                    Button(
                                        colors = ButtonDefaults.textButtonColors(),
                                        shape = RectangleShape,
                                        onClick = {
                                            showRpe = !showRpe
                                        }
                                    ) {
                                        Text(text = "rpe", color = if (showRpe) MaterialTheme.colorScheme.primary else TextFieldDefaults.colors().disabledTextColor)
                                    }
                                    Button(
                                        colors = ButtonDefaults.textButtonColors(),
                                        shape = RectangleShape,
                                        onClick = {
                                            showTempo = !showTempo
                                        }
                                    ) {
                                        Text(text = "tempo", color = if (showTempo) MaterialTheme.colorScheme.primary else TextFieldDefaults.colors().disabledTextColor)
                                    }
                                }
                            }
                        }
                    }

                    items(
                        state.items,
                        span = { item -> GridItemSpan(item.gridSize(state.items.size)) }
                    ) { item ->
                        when (item) {
                            is DashboardListItem.LiftCard -> {
                                when (val card = item as DashboardListItem.LiftCard) {
                                    is DashboardListItem.LiftCard.Loaded -> {
                                        LiftCard(
                                            showRpe = showRpe,
                                            showTempo = showTempo,
                                            state = card.state,
                                            onClick = { interactor(LiftClicked(card.state.lift.id)) },
                                            yUnit = showWeight.value
                                        )
                                    }

                                    DashboardListItem.LiftCard.Loading -> {
                                        Card(
                                            modifier = Modifier
                                                .aspectRatio(1f),
                                        ) {
                                        }
                                    }
                                }
                            }

                            DashboardListItem.ReleaseNotes -> {
                                ReleaseNotesRow(
                                    modifier = Modifier.height(72.dp)
                                )
                            }

                            DashboardListItem.AddLiftButton -> {
                                Box(

                                    modifier = Modifier.fillMaxSize()
                                        .then(
                                            if (item.gridSize(state.items.size) == 1) {
                                                Modifier.aspectRatio(
                                                    1f
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    Button(
                                        modifier = Modifier.align(Alignment.Center),
                                        onClick = {
                                            interactor(DashboardEvent.AddLiftClicked)
                                        },
                                        colors = ButtonDefaults.elevatedButtonColors()
                                    ) {
                                        Text("Add Lift")
                                    }
                                }
                            }
                        }
                    }

                    item(
                        span = { GridItemSpan(2) }
                    ) {
                        Text(
                            stringResource(
                                Res.string.dashboard_footer_version,
                                BuildKonfig.VERSION_NAME
                            )
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

private fun DashboardListItem.gridSize(listSize: Int = 0): Int = when (this) {
    is DashboardListItem.LiftCard -> 1
    DashboardListItem.ReleaseNotes -> 2
    DashboardListItem.AddLiftButton -> if (listSize % 2 == 0) 2 else 1
}
