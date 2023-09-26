package com.lift.bro.presentation.variation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import comliftbrodb.Lift
import kotlinx.coroutines.launch
import spacing

enum class LiftingUnit(val value: String) {
    KG("kg"),
    POUNDS("lbs")
}

@Composable
fun EditVariationScreen(
    id: String? = null,
    parentLiftId: String? = null,
    database: LBDatabase = dependencies.database,
    variationSaved: () -> Unit,
) {
    val variation = database.variantDataSource.get(id = id ?: "").executeAsOneOrNull()

    val parentLift = database.liftDataSource.liftQueries.get(variation?.liftId ?: parentLiftId ?: "")
        .executeAsOneOrNull()

    var lift by remember { mutableStateOf(parentLift) }

    var name by remember { mutableStateOf(variation?.name ?: "") }

    var personalBest by remember {
        mutableStateOf<Pair<Double?, LiftingUnit>>(
            Pair(
                null,
                LiftingUnit.POUNDS
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()

    LiftingScaffold(
        topBar = {
            TopBar(
                title = id?.let { "Edit Variation" } ?: "Create Variation",
                showBackButton = true
            )
        },
        fabText = "Save",
        fabClicked = {
            coroutineScope.launch {
                database.variantDataSource.save(
                    id = variation?.id ?: uuid4().toString(),
                    name = name,
                    liftId = lift?.id!!,
                    pbUnit = personalBest?.second?.value,
                    pbWeight = personalBest?.first
                )
                variationSaved()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

            LiftSelector(
                lift = lift,
                liftSelected = { lift = it }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Back Squat, Front Squat, Grip Style...") }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))

            WeightSelector(
                weight = personalBest,
                weightChanged = { personalBest = it }
            )
        }
    }
}

@Composable
fun WeightSelector(
    weight: Pair<Double?, LiftingUnit>,
    weightChanged: (Pair<Double?, LiftingUnit>) -> Unit,
) {
    Row {
        TextField(
            modifier = Modifier.weight(1f),
            value = weight.first?.toString() ?: "",
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
            label = {
                    Text("One Rep Max")
            },
            onValueChange = {
                if (it.toDoubleOrNull() != null) {
                    weightChanged(weight.copy(first = it.toDoubleOrNull()))
                }
            }
        )

        Button(
            onClick = {
                if (weight.second == LiftingUnit.POUNDS) {
                    weightChanged(
                        weight.copy(second = LiftingUnit.KG)
                    )
                } else {
                    weightChanged(
                        weight.copy(second = LiftingUnit.POUNDS)
                    )
                }
            }
        ) {
            Text(text = weight.second.value)
        }
    }
}

@Composable
fun LiftSelector(
    lift: Lift?,
    liftSelected: (Lift) -> Unit,
) {
    var showSelector by remember { mutableStateOf(false) }

    if (showSelector) {

        Dialog(
            onDismissRequest = { showSelector = false }
        ) {
            val lifts by dependencies.database.liftDataSource.getAll().collectAsState(emptyList())

            LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                contentPadding = PaddingValues(MaterialTheme.spacing.one)
            ) {
                items(lifts) { lift ->
                    LiftCard(
                        modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                        lift = lift,
                        onClick = {
                            showSelector = false
                            liftSelected(it)
                        }
                    )
                }
            }
        }
    }

    when (lift) {
        null -> {
            Card(
                modifier = Modifier.width(96.dp),
                onClick = { showSelector = true }
            ) {
                Text(
                    text = "Select Lift",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        else -> {
            LiftCard(
                modifier = Modifier.width(96.dp),
                lift = lift,
                onClick = { showSelector = true }
            )
        }
    }
}