package com.lift.bro.presentation.lift

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.spacing
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBarIconButton

private enum class Tab {
    Lifts,
    RecentSets,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LiftListScreen(
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {


    LiftingScaffold(
        title = "Lifts",
        actions = {
            TopBarIconButton(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Lift",
                onClick = addLiftClicked,
            )
        },
        fabIcon = Icons.Default.Add,
        contentDescription = "Add Set",
        fabClicked = addSetClicked,
    ) { padding ->
        val lifts by dependencies.database.liftDataSource.getAll().collectAsState(null)
        LazyVerticalGrid(
            modifier = Modifier.padding(padding),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
        ) {

            items(lifts ?: emptyList()) { lift ->
                LiftCard(
                    modifier = Modifier.padding(MaterialTheme.spacing.quarter).aspectRatio(1f),
                    lift = lift,
                    onClick = liftClicked
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}