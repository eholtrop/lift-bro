package com.lift.bro.presentation.variation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.ui.LiftSelector
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.WeightSelector
import kotlinx.coroutines.launch
import spacing

enum class UOM(val value: String) {
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
        mutableStateOf<Pair<Double?, UOM>>(
            Pair(
                null,
                UOM.POUNDS
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
