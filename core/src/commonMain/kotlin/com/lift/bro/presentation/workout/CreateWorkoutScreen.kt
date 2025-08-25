package com.lift.bro.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.maxText
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.Card
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.debug
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.filterEach
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import com.lift.bro.utils.today
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.excercise_screen_duplicate_cta
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    date: LocalDate,
) {
    val sets by dependencies.database.setDataSource.listenAll().collectAsState(emptyList())
    var workout by remember(date) { mutableStateOf(Workout(date = date, exercises = emptyList())) }
    var showVariationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sets) {
        workout = workout.copy(
            exercises = sets.filter { it.date.toLocalDate() == date }.groupBy { it.variationId }
                .map { (id, sets) ->
                    Exercise(
                        sets = sets,
                        variation = dependencies.database.variantDataSource.get(id)!!
                    )
                }
        )
    }


    LiftingScaffold(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Workout")
                Text(
                    date.toString("EEEE, MMMM d, yyyy"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one)
        ) {
            item {
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Add Warmup")
                    }

                    Space(MaterialTheme.spacing.half)

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {},
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Add Finisher")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clip(
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable(
                            onClick = {
                                showVariationDialog = true
                            },
                            role = Role.Button
                        ).padding(
                            horizontal = MaterialTheme.spacing.one,
                            vertical = MaterialTheme.spacing.half
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                    Space(MaterialTheme.spacing.half)
                    Text(
                        "Add Exercise",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(workout.exercises) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        val coordinator = LocalNavCoordinator.current
                        Column(
                            modifier = Modifier.fillMaxWidth().clickable(
                                onClick = {
                                    coordinator.present(Destination.VariationDetails(exercise.variation.id))
                                }
                            )
                                .padding(
                                    top = MaterialTheme.spacing.threeQuarters,
                                    start = MaterialTheme.spacing.one,
                                ),
                        ) {
                            Text(
                                exercise.variation.fullName,
                                style = MaterialTheme.typography.titleLarge,
                            )

                            Text(
                                exercise.variation.maxText(),
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            if (LocalTwmSettings.current) {
                                Text(
                                    "Total Weight Moved: ${"${exercise.totalWeightMoved.decimalFormat()} ${LocalUnitOfMeasure.current.value}"}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }

                            exercise.variation.notes?.let {
                                if (it.isNotBlank()) {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }


                        exercise.sets.forEach {
                            val coordinator = LocalNavCoordinator.current
                            SetInfoRow(
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp)
                                    .clickable(
                                        onClick = {
                                            coordinator.present(Destination.EditSet(setId = it.id))
                                        },
                                        role = Role.Button
                                    )
                                    .padding(
                                        horizontal = MaterialTheme.spacing.one,
                                        vertical = MaterialTheme.spacing.half
                                    ).animateItem(),
                                set = it
                            )
                        }

                        if (exercise.sets.isEmpty()) {
                            Button(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    coordinator.present(
                                        Destination.EditSet(
                                            variationId = exercise.variation.id,
                                            date = date.atStartOfDayIn(TimeZone.currentSystemDefault()),
                                        )
                                    )
                                }
                            ) {
                                Text("Warm Up")
                            }
                        } else {
                            Button(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    GlobalScope.launch {
                                        val baseSet =
                                            exercise.sets.maxByOrNull { it.date.toEpochMilliseconds() }

                                        if (baseSet != null) {
                                            dependencies.database.setDataSource.save(
                                                set = baseSet.copy(
                                                    id = uuid4().toString(),
                                                    // increment date by one to ensure this new list is the "Last Set"
                                                    date = baseSet.date.plus(
                                                        1,
                                                        DateTimeUnit.MILLISECOND
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            ) {
                                Text(stringResource(Res.string.excercise_screen_duplicate_cta))
                            }
                        }
                    }
                }
            }
        }
    }

    VariationSearchDialog(
        visible = showVariationDialog,
        onDismissRequest = {
            showVariationDialog = false
        },
        variationSelected = {
            showVariationDialog = false
            workout = workout.copy(
                exercises = workout.exercises + Exercise(
                    sets = emptyList(),
                    variation = it
                )
            )
        }
    )
}