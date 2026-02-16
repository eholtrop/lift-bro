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
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.lift.bro.data.datasource.flowToOneOrNull
import com.lift.bro.di.dependencies
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.models.Workout
import com.lift.bro.presentation.ApplicationScope
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.Space
import com.lift.bro.ui.calendar.Calendar
import com.lift.bro.ui.calendar.CalendarMonth
import com.lift.bro.ui.calendar.rememberCalendarState
import com.lift.bro.ui.calendar.today
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.fullName
import com.lift.bro.utils.maxDate
import com.lift.bro.utils.maxText
import com.lift.bro.utils.prettyPrintSet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_fab_content_description
import lift_bro.core.generated.resources.edit_daily_notes_dialog_confirm_cta
import lift_bro.core.generated.resources.edit_daily_notes_dialog_dismiss_cta
import lift_bro.core.generated.resources.edit_daily_notes_dialog_placeholder
import lift_bro.core.generated.resources.edit_daily_notes_dialog_title
import lift_bro.core.generated.resources.workout_calendar_edit_daily_notes_cta
import lift_bro.core.generated.resources.workout_calendar_screen_favourite_content_description
import lift_bro.core.generated.resources.workout_calendar_screen_finisher_label
import lift_bro.core.generated.resources.workout_calendar_screen_other_gains_subtitle
import lift_bro.core.generated.resources.workout_calendar_screen_other_gains_title
import lift_bro.core.generated.resources.workout_calendar_screen_start_workout_cta
import lift_bro.core.generated.resources.workout_calendar_screen_warmup_label
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.toColor
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.navi.LocalNavCoordinator
import tv.dpal.navi.NavCoordinator


@Composable
fun WorkoutCalendarContent(
    modifier: Modifier = Modifier,
    interactor: Interactor<WorkoutCalendarState, WorkoutCalendarEvent> = rememberWorkoutCalendarInteractor(),
    strings: WorkoutCalendarScreenStrings = WorkoutCalendarScreenStrings.default(),
) {
    val state by interactor.state.collectAsState()

    WorkoutCalendarContent(
        modifier = modifier,
        state = state,
        onEvent = { interactor(it) },
        strings = strings,
    )
}

@Composable
fun WorkoutCalendarContent(
    modifier: Modifier = Modifier,
    state: WorkoutCalendarState,
    onEvent: (WorkoutCalendarEvent) -> Unit = {},
    strings: WorkoutCalendarScreenStrings = WorkoutCalendarScreenStrings.default(),
) {

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
                    onEvent(
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
                        onEvent(
                            WorkoutCalendarEvent.DateSelected(it)
                        )
                    }
                )
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
            DailyWorkoutDetails(
                date = state.selectedDate,
                strings = strings,
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Serializable
data class DailyWorkoutDetailsState(
    val selectedDate: LocalDate,
    val log: LiftingLog?,
    val selectedWorkout: Workout?,
    val potentialExercises: List<Pair<Variation, List<LBSet>>>,
)

sealed interface DailyWorkoutDetailsEvent {
    data object CreateWorkoutClicked: DailyWorkoutDetailsEvent
    data class OpenWorkoutClicked(val exerciseId: ExerciseId?, val variationId: VariationId?):
        DailyWorkoutDetailsEvent

    data class AddToWorkout(val variationId: VariationId): DailyWorkoutDetailsEvent
}

@Composable
fun rememberDailyWorkoutDetailsInteractor(
    date: LocalDate,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): Interactor<DailyWorkoutDetailsState, DailyWorkoutDetailsEvent> =
    rememberInteractor<DailyWorkoutDetailsState, DailyWorkoutDetailsEvent>(
        initialState = DailyWorkoutDetailsState(
            selectedDate = date,
            log = null,
            selectedWorkout = null,
            potentialExercises = emptyList(),
        ),
        sideEffects = listOf(
            SideEffect { _, state, event ->
                when (event) {
                    is DailyWorkoutDetailsEvent.CreateWorkoutClicked -> navCoordinator.present(
                        Destination.CreateWorkout(state.selectedDate)
                    )

                    is DailyWorkoutDetailsEvent.OpenWorkoutClicked -> navCoordinator.present(
                        Destination.EditWorkout(
                            state.selectedDate
                        )
                    )

                    is DailyWorkoutDetailsEvent.AddToWorkout -> {
                        ApplicationScope.launch {
                            val exerciseId = uuid4().toString()
                            val workoutId = state.selectedWorkout?.id ?: uuid4().toString()
                            with(dependencies.workoutRepository) {
                                addVariation(exerciseId, event.variationId)
                                addExercise(workoutId, exerciseId)

                                save(
                                    Workout(
                                        workoutId,
                                        date = state.selectedDate
                                    )
                                )
                            }
                        }
                    }
                }
            }
        )
    ) { state ->
        combine(
            dependencies.workoutRepository.get(date),
            dependencies.database.logDataSource.getByDate(date).flowToOneOrNull(),
            FetchVariationSetsForRange(
                date,
                date
            )
        ) { workout, log, sets ->
            DailyWorkoutDetailsState(
                selectedDate = date,
                selectedWorkout = workout,
                log = log?.let {
                    LiftingLog(
                        id = it.id,
                        date = date,
                        notes = it.notes ?: "",
                        vibe = it.vibe_check?.toInt() ?: 0
                    )
                },
                potentialExercises = sets
                    .filter { vs -> (workout?.exercises ?: emptyList()).none { it.variationSets.any { it.variation.id == vs.first.id } } }
            )
        }
    }

@Composable
fun DailyWorkoutDetails(
    date: LocalDate,
    interactor: Interactor<DailyWorkoutDetailsState, DailyWorkoutDetailsEvent> = rememberDailyWorkoutDetailsInteractor(
        date
    ),
    strings: WorkoutCalendarScreenStrings = WorkoutCalendarScreenStrings.default()
) {
    val state by interactor.state.collectAsState()

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
                        Text(strings.notesSaveButton)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showNotesDialog = false
                        }
                    ) {
                        Text(strings.notesCancelButton)
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
                            Text(strings.notesPlaceholder)
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

    val workout = state.selectedWorkout
    when {
        workout == null || (workout.exercises.isEmpty() && workout.warmup.isNullOrBlank() && workout.finisher.isNullOrBlank()) -> {
            Button(
                onClick = {
                    interactor(DailyWorkoutDetailsEvent.CreateWorkoutClicked)
                },
                colors = ButtonDefaults.elevatedButtonColors()
            ) {
                Text(stringResource(Res.string.workout_calendar_screen_start_workout_cta))
            }
        }

        else -> {
            CalendarWorkoutCard(
                workout = workout,
                workoutClicked = { workout, date ->
                    interactor(DailyWorkoutDetailsEvent.OpenWorkoutClicked(null, null))
                },
            )
        }
    }

    if (state.potentialExercises.isNotEmpty()) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
        Text(
            text = stringResource(Res.string.workout_calendar_screen_other_gains_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(Res.string.workout_calendar_screen_other_gains_subtitle),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.half))

        Column(
            modifier = Modifier.fillMaxWidth().background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium
            )
                .clip(MaterialTheme.shapes.medium)
        ) {
            state.potentialExercises
                .forEach {
                    VariationSet(
                        modifier = Modifier.clickable(
                            onClick = {
                                interactor(
                                    DailyWorkoutDetailsEvent.AddToWorkout(
                                        variationId = it.first.id,
                                    )
                                )
                            },
                            role = Role.Button
                        ).padding(
                            horizontal = MaterialTheme.spacing.one,
                            vertical = MaterialTheme.spacing.half,
                        ),
                        variation = it.first,
                        sets = it.second,
                    )
                }
        }
    }

    Spacer(modifier = Modifier.height(72.dp))
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
                        workout.warmup?.let {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(Res.string.workout_calendar_screen_warmup_label))
                                Text(text = it)
                            }
                        }
                        workout.finisher?.let {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(Res.string.workout_calendar_screen_finisher_label))
                                Text(text = it)
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
                        .clip(MaterialTheme.shapes.small)
                ) {
                    Row {
                        exercise.variationSets.forEachIndexed { index, (_, variation, sets) ->
                            VariationSet(
                                modifier = Modifier.weight(1f),
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
}

private const val GRADIENT_SIZE = 50f

@Composable
fun VariationSet(
    modifier: Modifier = Modifier,
    variation: Variation,
    index: Int? = null,
    sets: List<LBSet>,
) {
    var width by remember { mutableStateOf(0f) }
    Row(
        modifier = modifier.fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        variation.lift?.color?.toColor() ?: Color.Transparent,
                        Color.Transparent,
                    ),
                    start = when (index) {
                        0 -> Offset.Zero
                        else -> Offset(Float.POSITIVE_INFINITY, 0f)
                    },
                    end = when (index) {
                        0 -> Offset(GRADIENT_SIZE, GRADIENT_SIZE)
                        else -> Offset(width - GRADIENT_SIZE, GRADIENT_SIZE)
                    }
                )
            )
            .graphicsLayer {
                width = this.size.width
            }
            .padding(MaterialTheme.spacing.half),
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
                        contentDescription = stringResource(
                            Res.string.workout_calendar_screen_favourite_content_description
                        )
                    )
                }
            }

            Text(
                "${variation.maxText()} - ${variation.maxDate?.toString("MMM - d")}",
                style = MaterialTheme.typography.labelMedium
            )

            val keySet = if (variation.bodyWeight == true) {
                sets.maxByOrNull { it.weight }
            } else {
                sets.maxByOrNull { it.reps }
            }

            if (keySet != null) {
                Text(
                    text = keySet.prettyPrintSet(),
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
    }
}

@Composable
fun WorkoutCalendarMonth(
    year: Int,
    month: Month,
    selectedDate: LocalDate? = null,
    dateSelected: (LocalDate) -> Unit,
) {
    val monthState by rememberWorkoutMonthInteractor(year, month).state.collectAsState()

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
                                top = 52.dp,
                                start = MaterialTheme.spacing.quarter,
                            )
                            .size(8.dp).align(Alignment.TopStart),
                        imageVector = Icons.Default.Edit,
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
                                    color = if (date == selectedDate || date == today) {
                                        LocalContentColor.current
                                    } else {
                                        color?.toColor()
                                            ?: LocalContentColor.current
                                    },
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
