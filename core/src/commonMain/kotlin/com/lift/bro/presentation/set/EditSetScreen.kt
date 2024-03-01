@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.lift.bro.Settings
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.formatDate
import com.lift.bro.presentation.spacing
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.VariationSelector
import com.lift.bro.ui.WeightSelector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun EditSetScreen(
    setId: String,
    variationId: String,
    setSaved: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var set by remember {
        mutableStateOf(
            dependencies.database.setDataSource.get(setId) ?: LBSet(
                id = uuid4().toString(),
                variationId = variationId,
            )
        )
    }

    val variation =
        dependencies.database.variantDataSource.get(set.variationId)

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

            DateSelector(
                date = set.date,
                dateChanged = { set = set.copy(date = it) }
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
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DateSelector(
    modifier: Modifier = Modifier,
    date: Instant,
    dateChanged: (Instant) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }

    val pickerState = rememberDatePickerState(date.toEpochMilliseconds())

    if (openDialog) {
        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        dateChanged(
                            Instant.fromEpochMilliseconds(pickerState.selectedDateMillis!!)
                        )
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDialog = false }
                ) {
                    Text("Close")
                }
            }
        ) {
            DatePicker(
                state = pickerState
            )
        }
    }

    LineItem(
        modifier = modifier,
        title = "Set Date",
        description = Instant.fromEpochMilliseconds(pickerState.selectedDateMillis!!).formatDate("MMMM d - yyyy"),
        onClick = {
            openDialog = true
        }
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
            modifier = Modifier.weight(.25f).height(52.dp),
            title = "Reps",
            selectedNum = reps,
            numberChanged = repChanged
        )
        NumberPicker(
            modifier = Modifier.weight(.25f).height(52.dp),
            title = "Down",
            selectedNum = down,
            numberChanged = downChanged
        )
        NumberPicker(
            modifier = Modifier.weight(.25f).height(52.dp),
            title = "Hold",
            selectedNum = hold,
            numberChanged = holdChanged
        )
        NumberPicker(
            modifier = Modifier.weight(.25f).height(52.dp),
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

        TextField(
            modifier = modifier,
            value = selectedNum.toString(),
            onValueChange = { numberChanged(it.toInt()) },
            label = {
                Text(title)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            )
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