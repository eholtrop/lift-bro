package com.lift.bro.presentation.set

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.lift.bro.Settings
import com.lift.bro.data.Set
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
import kotlinx.datetime.LocalDate
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
            dependencies.database.setDataSource.get(setId) ?: Set(
                id = uuid4().toString(),
                variationId = variationId,
            )
        )
    }

    val variation = dependencies.database.variantDataSource.get(set.variationId).executeAsOneOrNull()

    LiftingScaffold(
        fabText = "Create Set",
        fabEnabled = variation != null,
        fabClicked = {
            coroutineScope.launch {
                dependencies.database.setDataSource.save(set)
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
                variationSelected = {
                    set = set.copy(variationId = it.id)
                },
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))

            WeightSelector(
                weight = Pair(set.weight, Settings.defaultUOM),
                liftId = variation?.liftId ?: "",
                placeholder = "Weight",
                weightChanged = { set = set.copy(weight = it.first ?: 0.0) }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))

            TempoSelector(
                reps = set.reps.toInt(),
                up = set.tempoUp.toInt(),
                hold = set.tempoHold.toInt(),
                down = set.tempoDown.toInt(),
                repChanged = { set = set.copy(reps = it.toLong()) },
                downChanged = { set = set.copy(tempoDown = it.toLong()) },
                holdChanged = { set = set.copy(tempoHold = it.toLong()) },
                upChanged = { set = set.copy(tempoUp = it.toLong()) },
            )

            DateSelector(
                date = set.date,
                dateChanged = { set = set.copy(date = it) }
            )
        }
    }
}

@Composable
fun DateSelector(
    modifier: Modifier = Modifier,
    date: LocalDate,
    dateChanged: (LocalDate) -> Unit
) {
    LineItem(
        title = "Set Date",
        description = date.toString(),
        onClick = {}
    )
}


@Composable
fun LineItem(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .clickable(
                enabled = true,
                onClick = onClick,
                role = Role.Button,
            )
            .padding(
                vertical = MaterialTheme.spacing.quarter,
                horizontal = MaterialTheme.spacing.one
            )
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        title?.let {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TempoSelector(
    modifier: Modifier = Modifier,
    reps: Int,
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
            selectedNum = reps,
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