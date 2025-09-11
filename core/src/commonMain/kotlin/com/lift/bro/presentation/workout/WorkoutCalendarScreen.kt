package com.lift.bro.presentation.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.di.dependencies
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.maxText
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.ads.AdBanner
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.Calendar
import com.lift.bro.ui.CalendarMonth
import com.lift.bro.ui.Space
import com.lift.bro.ui.currentMonth
import com.lift.bro.ui.rememberCalendarState
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.lift.bro.utils.mapEach
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.edit_daily_notes_dialog_confirm_cta
import lift_bro.core.generated.resources.edit_daily_notes_dialog_dismiss_cta
import lift_bro.core.generated.resources.edit_daily_notes_dialog_placeholder
import lift_bro.core.generated.resources.edit_daily_notes_dialog_title
import lift_bro.core.generated.resources.workout_calendar_edit_daily_notes_cta
import org.jetbrains.compose.resources.stringResource

@Composable
fun WorkoutCalendarContent(
    modifier: Modifier = Modifier,
    interactor: Interactor<WorkoutCalendarState, WorkoutCalendarEvent> = rememberWorkoutCalendarInteractor(),
) {
    val state by interactor.state.collectAsState()
    val subscriptionType by LocalSubscriptionStatusProvider.current

    val calendarState = rememberCalendarState()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {
            Calendar(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight(),
                selectedDate = state.selectedDate,
                contentPadding = PaddingValues(0.dp),
                dateSelected = {
                    interactor(
                        WorkoutCalendarEvent.DateSelected(it)
                    )
                },
                pagerState = calendarState,
                dateDecorations = { date, decoration -> }
            ) { year, month ->
                WorkoutCalendarMonth(
                    year,
                    month,
                    selectedDate = state.selectedDate,
                    dateSelected = {
                        interactor(
                            WorkoutCalendarEvent.DateSelected(it)
                        )
                    }
                )
            }
        }


        if (subscriptionType == SubscriptionType.None) {
            item {
                AdBanner(modifier = Modifier.defaultMinSize(minHeight = 52.dp).fillMaxWidth())
            }
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(height = 2.dp)
                    .background(MaterialTheme.colorScheme.surfaceDim)
            ) {}
        }

        item {
            Column {
                var showNotesDialog by remember { mutableStateOf(false) }
                var todaysNotes by remember { mutableStateOf(state.log?.notes ?: "") }

                if (showNotesDialog) {
                    AlertDialog(
                        onDismissRequest = { showNotesDialog = false },
                        confirmButton = {
                            Button(
                                onClick = {
                                    GlobalScope.launch {
                                        dependencies.database.logDataSource.save(
                                            id = state.log?.id ?: uuid4().toString(),
                                            date = state.log?.date ?: state.selectedDate,
                                            notes = todaysNotes,
                                            vibe_check = state.log?.vibe?.toLong()
                                        )
                                        showNotesDialog = false
                                    }
                                }
                            ) {
                                Text(stringResource(Res.string.edit_daily_notes_dialog_confirm_cta))
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showNotesDialog = false
                                }
                            ) {
                                Text(stringResource(Res.string.edit_daily_notes_dialog_dismiss_cta))
                            }
                        },
                        title = {
                            Text(
                                stringResource(
                                    Res.string.edit_daily_notes_dialog_title,
                                    state.selectedDate.toString("EEEE, MMM d - yyyy")
                                )
                            )
                        },
                        text = {
                            val focusRequester = FocusRequester()
                            TextField(
                                modifier = Modifier.defaultMinSize(minHeight = 128.dp)
                                    .focusRequester(focusRequester),
                                value = todaysNotes,
                                onValueChange = { todaysNotes = it },
                                placeholder = {
                                    Text(stringResource(Res.string.edit_daily_notes_dialog_placeholder))
                                }
                            )

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.animateContentSize().fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        text = state.selectedDate.toString("EEEE, MMM d - yyyy"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = {
                            showNotesDialog = true
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.workout_calendar_edit_daily_notes_cta)
                        )
                    }
                }

                state.log?.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        item {
            when (val workout = state.selectedWorkout) {
                null -> {
                    Button(
                        onClick = {
                            Log.d(message = "add workout clicked")
                            interactor(WorkoutCalendarEvent.AddWorkoutClicked(state.selectedDate))
                        },
                        colors = ButtonDefaults.elevatedButtonColors()
                    ) {
                        Text("Start a Workout!")
                    }
                }

                else -> {
                    CalendarWorkoutCard(
                        modifier = Modifier.animateItem(),
                        workout = workout,
                        workoutClicked = { workout, date ->
                            interactor(WorkoutCalendarEvent.WorkoutClicked(workout))
                        },
                    )
                }
            }
        }

        if (state.potentialExercises.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        text = "Other Gains!! Tap to add to Workout",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        state.potentialExercises
            .forEach {
                item {
                    VariationSet(
                        modifier = Modifier.clickable(
                            onClick = {
                                interactor(
                                    WorkoutCalendarEvent.AddToWorkout(
                                        date = state.selectedDate,
                                        variation = it.first
                                    )
                                )
                            },
                            role = Role.Button
                        ),
                        variation = it.first,
                        sets = it.second,
                    )
                }
            }


        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun CalendarWorkoutCard(
    modifier: Modifier = Modifier,
    workout: Workout,
    workoutClicked: (Workout, LocalDate) -> Unit,
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.clickable(
                onClick = { workoutClicked(workout, workout.date) }
            )
                .padding(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
        ) {
            if (workout.warmup != null || workout.finisher != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelMedium,
                    ) {
                        if (workout.warmup != null) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Warmup:")
                                Text(text = workout.warmup)
                            }
                        }
                        if (workout.finisher != null) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Finisher:")
                                Text(text = workout.finisher)
                            }
                        }
                    }
                }
            }

            workout.exercises.forEach { exercise ->
                Column(
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small,
                    )
                        .padding(MaterialTheme.spacing.half)
                ) {
                    exercise.variationSets.forEachIndexed { index, (_, variation, sets) ->
                        VariationSet(
                            index = if (exercise.variationSets.size > 1) index else null,
                            variation = variation,
                            sets = sets
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VariationSet(
    modifier: Modifier = Modifier,
    variation: Variation,
    index: Int? = null,
    sets: List<LBSet>,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val prefix = index?.let { 'A'.plus(index).plus(".") } ?: ""

                Text(
                    "$prefix ${variation.fullName}".trim(),

                    style = MaterialTheme.typography.titleMedium
                )
                if (variation.favourite) {
                    Space(MaterialTheme.spacing.half)
                    Icon(
                        modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp),
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favourite"
                    )
                }
            }
            Text(
                variation.maxText(),
                style = MaterialTheme.typography.bodyMedium
            )
            val differingWeights =
                sets.any { set -> sets.firstOrNull()?.weight != set.weight }

            val keySet = if (differingWeights) {
                sets.maxByOrNull { it.weight }
            } else {
                sets.maxByOrNull { it.reps }
            }

            if (keySet != null) {
                Text(
                    text = "${weightFormat(keySet.weight)} x ${keySet.reps}",
                    style = MaterialTheme.typography.bodyMedium
                )
                keySet.tempo.render()
                if (keySet.notes.isNotBlank()) {
                    Text(
                        keySet.notes,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Box(
            modifier = Modifier.background(
                color = variation.lift?.color?.toColor()
                    ?: MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ).height(MaterialTheme.spacing.oneAndHalf).aspectRatio(1f),
            content = {}
        )
    }
}

@Composable
fun WorkoutCalendarMonth(
    year: Int,
    month: Month,
    selectedDate: LocalDate? = null,
    dateSelected: (LocalDate) -> Unit,
) {
    val monthState by rememberInteractor<WorkoutMonthState, WorkoutCalendarEvent>(
        initialState = WorkoutMonthState(year, month),
        source = {
            combine(
                dependencies.workoutRepository.getAll(
                    LocalDate(year, month, 1),
                    LocalDate(year, month, 1)
                        .plus(1, DateTimeUnit.MONTH),
                ).mapEach { workout ->
                    workout.date to workout.exercises.map { it.variationSets.map { it.variation.lift?.color } }
                        .flatten()
                },
                FetchVariationSetsForRange(
                    year,
                    month,
                ).map {
                    it.groupBy { it.second.firstOrNull()?.date?.toLocalDate() }
                        .mapValues { entry ->
                            entry.value.map { it.first.lift?.color }
                        }
                        .toList()

                },
                dependencies.database.logDataSource.getAll().flowToList(),
            ) { workouts, unallocatedSets, logs ->
                WorkoutMonthState(
                    year = year,
                    month = month,
                    colors = (workouts + unallocatedSets).toMap(),
                    logs = logs.map {
                        LiftingLog(
                            id = it.id,
                            date = it.date,
                            notes = it.notes ?: "",
                            vibe = it.vibe_check?.toInt()
                        )
                    }.associateBy { it.date },
                )
            }

        }
    ).state.collectAsState()


    CalendarMonth(
        year = year,
        month = month,
        selection = selectedDate,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
        dateSelected = dateSelected,
        dateDecorations = { date, day ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (monthState.logs[date] != null) {
                    Icon(
                        modifier = Modifier
                            .padding(
                                top = MaterialTheme.spacing.quarter,
                                start = MaterialTheme.spacing.quarter,
                            )
                            .size(8.dp).align(Alignment.TopStart),
                        imageVector = Icons.Default.Edit,
                        tint = if (date == selectedDate) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    day()
                    Space(MaterialTheme.spacing.quarter)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                    ) {
                        monthState.colors[date]?.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier.background(
                                    color = if (date == selectedDate) MaterialTheme.colorScheme.onPrimary else color?.toColor()
                                        ?: MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                ).size(4.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}