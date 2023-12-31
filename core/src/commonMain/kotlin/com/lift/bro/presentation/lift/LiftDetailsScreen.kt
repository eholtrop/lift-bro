package com.lift.bro.presentation.lift

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.Set
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.variation.formattedMax
import com.lift.bro.presentation.voyager.EditLiftVoyagerScreen
import com.lift.bro.presentation.voyager.EditVariationVoyagerScreen
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton
import comliftbrodb.LiftingSet
import comliftbrodb.Variation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.map
import spacing

@Composable
fun LiftDetailsScreen(
    liftId: String,
    editLiftClicked: () -> Unit,
    addVariationClicked: () -> Unit,
    variationClicked: (String) -> Unit,
    addSetClicked: () -> Unit,
    database: LBDatabase = dependencies.database,
) {
    val lift by database.liftDataSource.get(liftId).collectAsState(null)

    val variations by database.variantDataSource.getAll(liftId).asFlow().mapToList(Dispatchers.IO)
        .collectAsState(emptyList())

    lift?.let { lift ->
        LiftingScaffold(
            fabText = "Add Set",
            fabClicked = addSetClicked,
            topBar = {
                TopBar(
                    title = lift.name,
                    showBackButton = true,
                    trailingContent = {
                        TopBarIconButton(
                            Icons.Default.Add,
                            contentDescription = "Add Variation",
                            onClick = addVariationClicked,
                        )
                        TopBarIconButton(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            onClick = editLiftClicked,
                        )
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(variations) { variation ->
                    VariationCard(
                        variation = variation,
                        onClick = { variationClicked(variation.id) }
                    )
                }

            }
        }
    }
}

@Composable
fun VariationCard(
    variation: Variation,
    onClick: (Variation) -> Unit,
) {

    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.one
            ),
        onClick = { onClick(variation) }
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(variation.name ?: "")

            val maxLift = dependencies.database.setDataSource.getAll(variation.id)
                .fold(null as Set?) { maxLift, currentSet ->
                    when {
                        maxLift == null || maxLift.weight < currentSet.weight -> currentSet
                        else -> maxLift
                    }
                }

            maxLift?.let { lift ->
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.quarter))
                if (lift.weight != null) {
                    Text(lift.formattedMax)
                } else {
                    Text("No Max")
                }
            } ?: run {
                Text("No Max")
            }
        }
    }
}