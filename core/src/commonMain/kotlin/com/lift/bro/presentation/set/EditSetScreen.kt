@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.set

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.lift.bro.Settings
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.fullName
import com.lift.bro.presentation.dialog.CreateVariationDialog
import com.lift.bro.presentation.navigation.LocalNavCoordinator
import com.lift.bro.presentation.theme.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.VariationCard
import com.lift.bro.ui.WeightSelector
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.formattedWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private data class EditSetState(
    val id: String,
    val variationId: String? = null,
    val weight: Double = 0.0,
    val reps: Long? = 1,
    val down: Long? = 3,
    val hold: Long? = 1,
    val up: Long? = 1,
    val date: Instant = Clock.System.now(),
    val notes: String = "",
)

private fun LBSet.toUiState() = EditSetState(
    id = this.id,
    variationId = this.variationId,
    weight = this.weight,
    reps = this.reps,
    down = this.tempo.down,
    hold = this.tempo.hold,
    up = this.tempo.up,
    date = this.date,
    notes = this.notes,
)

private fun EditSetState.toDomain() = LBSet(
    id = this.id,
    variationId = this.variationId!!,
    weight = this.weight,
    reps = this.reps!!,
    tempo = Tempo
        (
        down = this.down!!,
        hold = this.hold!!,
        up = this.up!!
    ),
    date = this.date,
    notes = this.notes,
)

@Composable
fun EditSetScreen(
    setId: String?,
    variationId: String?,
    liftId: String?,
    setSaved: () -> Unit,
    createLiftClicked: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var set by remember {
        mutableStateOf(
            dependencies.database.setDataSource.get(setId)?.toUiState() ?: EditSetState(
                id = uuid4().toString(),
                variationId = variationId,
            )
        )
    }

    val variation =
        dependencies.database.variantDataSource.get(set.variationId)

    val saveEnabled =
        set.variationId != null && set.reps != null && set.down != null && set.hold != null && set.up != null && set.weight != null

    LiftingScaffold(
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Edit,
            contentDescription = "Save Set",
            fabEnabled = saveEnabled,
            fabClicked = {
                coroutineScope.launch {
                    dependencies.database.setDataSource.save(set.toDomain())
                }
                setSaved()
            },
        ),
        title = "${if (setId != null) "You" else "I"} Crushed...",
        trailingContent = {
            if (setId != null) {
                val navCoordinator = LocalNavCoordinator.current
                TopBarIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    onClick = {
                        coroutineScope.launch {
                            dependencies.database.setDataSource.delete(setId)
                            navCoordinator.onBackPressed()
                        }
                    }
                )
            }
        },
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                NumberPicker(
                    modifier = Modifier.animateContentSize().wrapContentSize(),
                    selectedNum = set.reps?.toInt(),
                    title = null,
                    numberChanged = { set = set.copy(reps = it?.toLong()) },
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    suffix = {
                        Text("reps")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.oneAndHalf))
            }

            item {
                Card(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
                    border = BorderStroke(width = Dp.Hairline, color = Color.Black),
                ) {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .clickable(
                                enabled = set.variationId != null,
                                onClick = {
                                    set = set.copy(variationId = null)
                                },
                                role = Role.DropdownList
                            )
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Space(MaterialTheme.spacing.one)
                        Text(
                            text = variation?.fullName ?: "Select Lift",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Space(MaterialTheme.spacing.one)
                        if (variation == null) {
                            VariationSelector(
                                variationSelected = { set = set.copy(variationId = it) },
                                initialLiftId = liftId,
                            )
                            Button(
                                onClick = createLiftClicked,
                            ) {
                                Text("Create Lift")
                            }
                            Space(MaterialTheme.spacing.one)
                        } else {
                            val sets = dependencies.database.setDataSource.getAllForLift(
                                variation.lift?.id ?: ""
                            )

                            val liftMax = sets.maxByOrNull { it.weight }
                            val variationMax = sets.filter { it.variationId == variation.id }
                                .maxByOrNull { it.weight }
                            Text("${variation.lift?.name ?: ""} Max: ${liftMax?.formattedWeight ?: "None"}")
                            Text("${variation.fullName} Max: ${variationMax?.formattedWeight ?: "None"}")
                            Space(MaterialTheme.spacing.half)
                        }
                    }
                }
            }

            if (variation != null) {
                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
                    WeightSelector(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
                        weight = Pair(set.weight, Settings.defaultUOM),
                        variation = variation,
                        placeholder = "At",
                        weightChanged = { set = set.copy(weight = it.first ?: 0.0) }
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

                    TempoSelector(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
                        up = set.up?.toInt(),
                        hold = set.hold?.toInt(),
                        down = set.down?.toInt(),
                        downChanged = { set = set.copy(down = it?.toLong()) },
                        holdChanged = { set = set.copy(hold = it?.toLong()) },
                        upChanged = { set = set.copy(up = it?.toLong()) },
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

                    DateSelector(
                        date = set.date,
                        dateChanged = { set = set.copy(date = it) }
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.one),
                    ) {
                        Text("Extra Notes:")

                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = set.notes,
                            singleLine = true,
                            placeholder = {
                                Text("I killed it today!")
                            },
                            onValueChange = {
                                set = set.copy(notes = it)
                            },
                        )
                    }
                }
            }
        }
    }
}
@Composable
internal fun VariationSelector(
    modifier: Modifier = Modifier,
    initialLiftId: String?,
    variationSelected: (String) -> Unit,
) {
    val lifts by dependencies.database.variantDataSource.listenAll().map {
        it.groupBy { it.lift }
            .toList()
            .sortedBy { it.first!!.id }
    }.collectAsState(emptyList())

    var expandedLift: Lift? by remember { mutableStateOf(null) }

    LaunchedEffect(lifts) {
        if (initialLiftId != null) {
            expandedLift = lifts.firstOrNull { it.first?.id == initialLiftId }?.first
        }
    }

    var showCreateVariationDialog: Boolean by remember { mutableStateOf(false) }

    if (showCreateVariationDialog) {
        CreateVariationDialog(
            parentLiftId = expandedLift!!.id,
            onDismissRequest = { showCreateVariationDialog = false },
            onVariationCreated = { variationSelected(it) }
        )
    }

    Column(
        modifier = modifier.padding(horizontal = MaterialTheme.spacing.half),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        lifts.forEach { map ->
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = Dp.AccessibilityMinimumSize)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        role = Role.Button,
                        onClick = { expandedLift = map.first },
                    )
                    .padding(horizontal = MaterialTheme.spacing.half),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f),
                    text = map.first?.name ?: "",
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (map.first == expandedLift) {
                map.second.chunked(2).forEach { variations ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                    ) {
                        variations.forEach { variation ->
                            VariationCard(
                                modifier = Modifier.padding(MaterialTheme.spacing.quarter)
                                    .weight(1f),
                                variation = variation,
                                onClick = { variationSelected(variation.id) }
                            )
                        }
                    }
                }

                Button(
                    modifier = Modifier.wrapContentWidth().minimumInteractiveComponentSize(),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    onClick = {
                        showCreateVariationDialog = true
                    }
                ) {
                    Text("Create ${map.first?.name ?: ""} Variation")
                }
            }
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

    val pickerState = rememberDatePickerState(
        date.toLocalDateTime(TimeZone.currentSystemDefault()).toInstant(
            TimeZone.UTC
        ).toEpochMilliseconds()
    )

    if (openDialog) {
        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        dateChanged(
                            Instant.fromEpochMilliseconds(pickerState.selectedDateMillis!!)
                                .toLocalDateTime(TimeZone.UTC)
                                .toInstant(TimeZone.currentSystemDefault())
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
        title = "On",
        description = Instant.fromEpochMilliseconds(pickerState.selectedDateMillis!!)
            .toLocalDateTime(TimeZone.UTC)
            .toInstant(TimeZone.currentSystemDefault())
            .toString("MMMM d - yyyy"),
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
    down: Int?,
    hold: Int?,
    up: Int?,
    downChanged: (Int?) -> Unit,
    holdChanged: (Int?) -> Unit,
    upChanged: (Int?) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
    ) {
        Text("With a tempo of...")

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = "Down",
                selectedNum = down,
                numberChanged = downChanged
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = "Hold",
                selectedNum = hold,
                numberChanged = holdChanged
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = "Up",
                selectedNum = up,
                numberChanged = upChanged,
                imeAction = ImeAction.Done
            )
        }
    }
}

@Composable
fun NumberPicker(
    modifier: Modifier,
    title: String?,
    selectedNum: Int? = null,
    numberChanged: (Int?) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    textStyle: TextStyle = LocalTextStyle.current,
    suffix: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        var value by remember { mutableStateOf(TextFieldValue(selectedNum?.toString() ?: "")) }

        var focus by remember { mutableStateOf(false) }

        if (focus) {
            LaunchedEffect(focus) {
                if (focus) {
                    value = value.copy(selection = TextRange(0, value.text.length))
                }
            }
        }

        TextField(
            modifier = Modifier.onFocusChanged {
                focus = it.isFocused
            },
            suffix = suffix,
            value = value,
            onValueChange = {
                numberChanged(it.text.toIntOrNull())
                value = it
            },
            label = title?.let {
                {
                    Text(title)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction,
            ),
            textStyle = textStyle,
        )
    }
}
