package com.lift.bro.presentation.set

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.variation.UOM
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Picker
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.VariationSelector
import com.lift.bro.ui.WeightSelector
import comliftbrodb.LiftingSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import spacing

@Composable
fun EditSetScreen(
    setId: String,
    variationId: String,
    setSaved: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var set by remember {
        mutableStateOf(
            dependencies.database.setDataSource.get(setId)
                .executeAsOneOrNull() ?: LiftingSet(
                id = uuid4().toString(),
                variationId = variationId,
                weight = null,
                unit = null,
                reps = null,
                tempoDown = null,
                tempoHold = null,
                tempoUp = null
            )
        )
    }

    var variation by remember {
        mutableStateOf(
            dependencies.database.variantDataSource.get(set.variationId)
                .executeAsOneOrNull()
        )
    }

    LiftingScaffold(
        fabText = "Create Set",
        fabClicked = {
            coroutineScope.launch {
                dependencies.database.setDataSource.save(
                    id = set.id,
                    variationId = set.variationId,
                    weight = set.weight,
                    unit = set.unit,
                    reps = set.reps,
                    tempoDown = set.tempoDown,
                    tempoHold = set.tempoHold,
                    tempoUp = set.tempoUp
                )
            }
            setSaved()
        },
        topBar = {
            TopBar(
                title = "",
                showBackButton = true,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VariationSelector(
                variation = variation,
                variationSelected = { variation = it }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))

            WeightSelector(
                weight = Pair(set?.weight, UOM.valueOf(set?.unit ?: UOM.POUNDS.toString())),
                placeholder = "Weight",
                weightChanged = { set = set.copy(weight = it.first, unit = it.second.toString()) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))

            TempoSelector(
                up = 1,
                hold = 1,
                down = 3,
                repChanged = { set = set.copy(reps = it.toLong()) },
                downChanged = { set = set.copy(tempoDown = it.toLong()) },
                holdChanged = { set = set.copy(tempoHold = it.toLong()) },
                upChanged = { set = set.copy(tempoUp = it.toLong()) },
            )
        }
    }
}

@Composable
fun TempoSelector(
    modifier: Modifier = Modifier,
    down: Int,
    hold: Int,
    up: Int,
    repChanged: (Int) -> Unit,
    downChanged: (Int) -> Unit,
    holdChanged: (Int) -> Unit,
    upChanged: (Int) -> Unit,
) {
    Row(
        modifier = modifier,
    ) {

        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Reps",
            selectedNum = 0,
            numberChanged = repChanged
        )
        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Down",
            selectedNum = down,
            numberChanged = downChanged
        )
        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Hold",
            selectedNum = hold,
            numberChanged = holdChanged
        )
        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Up",
            selectedNum = up,
            numberChanged = upChanged
        )
    }
}

@Composable
fun NumberPicker(
    modifier: Modifier,
    title: String,
    selectedNum: Int = 0,
    numberChanged: (Int) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(text = title)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.quarter))
        Picker(
            modifier = Modifier.weight(.33f),
            items = (1..99).toList().map { it.toString() },
            startIndex = selectedNum - 1,
            selectedItemChanged = { numberChanged(it.toInt()) }
        )
    }
}

enum class TimeUnit {
    Minutes, Hours;
}

@Composable
fun RestSelector(
    time: Int,
    unit: TimeUnit,
) {

}