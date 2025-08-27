@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.ads.AdBanner
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftCardYValue
import com.lift.bro.ui.ReleaseNotesRow
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_footer_version
import lift_bro.core.generated.resources.reps
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    viewModel: DashboardViewModel = rememberDashboardViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val showWeight = LocalLiftCardYValue.current
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
    ) {
        item(
            span = { GridItemSpan(2) }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = {
                        showWeight.value =
                            if (showWeight.value == LiftCardYValue.Weight) LiftCardYValue.Reps else LiftCardYValue.Weight
                    }
                ) {
                    Text(
                        text = if (showWeight.value == LiftCardYValue.Weight) LocalUnitOfMeasure.current.value else stringResource(
                            Res.string.reps
                        )
                    )
                }
            }
        }

        items(
            state.items,
            span = { item -> GridItemSpan(item.gridSize()) }
        ) { item ->
            when (val state = item) {
                DashboardListItem.Ad -> {
                    AdBanner(
                        modifier = Modifier.defaultMinSize(
                            minHeight = 52.dp
                        )
                    )
                }

                is DashboardListItem.LiftCard -> {
                    LiftCard(
                        state = state.state,
                        onClick = liftClicked,
                        value = showWeight.value
                    )
                }

                DashboardListItem.ReleaseNotes -> {
                    ReleaseNotesRow(
                        modifier = Modifier.height(72.dp)
                    )
                }
            }
        }

        val addButtonGridSize =
            if (state.items.size % 2 == 0) GridItemSpan(1) else GridItemSpan(2)
        item(
            span = { addButtonGridSize }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .then(
                        if (addButtonGridSize.currentLineSpan == 1) Modifier.aspectRatio(
                            1f
                        ) else Modifier
                    )
            ) {
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = addLiftClicked,
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Add Lift")
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

private fun DashboardListItem.gridSize(): Int = when (this) {
    DashboardListItem.Ad -> 2
    is DashboardListItem.LiftCard -> 1
    DashboardListItem.ReleaseNotes -> 2
}