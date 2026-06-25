@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Workout
import com.lift.bro.presentation.LocalNavCoordinator
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.category.WarningDialog
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.card.lift.weightFormat
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.ThemePreviews
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.maxText
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.workout_add_exercise_cta
import lift_bro.core.generated.resources.workout_notes_placeholder
import lift_bro.core.generated.resources.workout_screen_add_set_content_description
import lift_bro.core.generated.resources.workout_screen_again_content_description
import lift_bro.core.generated.resources.workout_screen_copy_recent_workout_subtitle
import lift_bro.core.generated.resources.workout_screen_copy_recent_workout_title
import lift_bro.core.generated.resources.workout_screen_delete_variation_content_description
import lift_bro.core.generated.resources.workout_screen_delete_warning_text
import lift_bro.core.generated.resources.workout_screen_duplicate_last_set_content_description
import lift_bro.core.generated.resources.workout_screen_next_content_description
import lift_bro.core.generated.resources.workout_screen_previous_lift_content_description
import lift_bro.core.generated.resources.workout_screen_superset_cta
import lift_bro.core.generated.resources.workout_screen_title
import lift_bro.core.generated.resources.workout_set_options_copy_cta
import lift_bro.core.generated.resources.workout_set_options_delete_cta
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.AccessibilityMinimumSize
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import kotlin.math.absoluteValue

@Composable
fun WorkoutScreen(
    interactor: Interactor<CreateWorkoutState, CreateWorkoutEvent>,
) {
    val state by interactor.state.collectAsState()

    WorkoutScreenInternal(
        state = state,
        eventHandler = { interactor(it) }
    )
}

sealed class VariationDialogReason {
    object AddExercise: VariationDialogReason()
    data class Superset(val exercise: ExerciseItem): VariationDialogReason()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreenInternal(
    state: CreateWorkoutState,
    eventHandler: (CreateWorkoutEvent) -> Unit = {},
) {
    var showVariationDialog by remember { mutableStateOf<VariationDialogReason?>(null) }

    LiftingScaffold(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(Res.string.workout_screen_title))
                Text(
                    state.date.toString("EEEE, MMMM d, yyyy"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one),
        ) {
            item {
                var notes by remember(state.notes) { mutableStateOf(state.notes) }

                TextField(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.one),
                    value = notes,
                    placeholder = { Text(stringResource(Res.string.workout_notes_placeholder)) },
                    onValueChange = {
                        notes = it
                        eventHandler(CreateWorkoutEvent.UpdateNotes(it))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null
                        )
                    }
                )
            }

            item {
                WarmupFinisherRow(
                    warmup = state.warmup,
                    finisher = state.finisher,
                    eventHandler = eventHandler
                )
            }

            item {
                AddExerciseRow {
                    showVariationDialog = VariationDialogReason.AddExercise
                }
            }

            if (state.exercises.isEmpty() && state.recentWorkouts.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = MaterialTheme.spacing.one),
                    ) {
                        Text(
                            text = stringResource(Res.string.workout_screen_copy_recent_workout_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(Res.string.workout_screen_copy_recent_workout_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                items(
                    items = state.recentWorkouts,
                    key = { it.id }
                ) { workout ->
                    RecentWorkoutCard(
                        modifier = Modifier.fillMaxWidth()
                            .animateItem()
                            .padding(horizontal = MaterialTheme.spacing.one),
                        workout = workout,
                        recentWorkoutClicked = {
                            eventHandler(CreateWorkoutEvent.CopyWorkout(it))
                        }
                    )
                }
            }

            itemsIndexed(
                items = state.exercises,
                key = { _, it -> it.id },
            ) { index, exercise ->
                val pagerState = rememberPagerState(pageCount = { exercise.sections.size })
                val coroutineScope = rememberCoroutineScope()
                HorizontalPager(
                    modifier = Modifier.animateItem(),
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one),
                    pageSpacing = MaterialTheme.spacing.two.times(-2),
                ) { page ->
                    val vSets = exercise.sections[page]

                    WorkoutSectionCard(
                        modifier = Modifier
                            .animateItem()
                            .variationCardAnimation(pagerState, page),
                        section = vSets,
                        eventHandler = eventHandler,
                        index = if (pagerState.pageCount > 1) page else null,
                        date = state.date
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            exercise.sections.getOrNull(page - 1)?.let { previous ->
                                FooterButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ChevronLeft,
                                            contentDescription = stringResource(
                                                Res.string.workout_screen_previous_lift_content_description
                                            )
                                        )
                                    }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        previous.sets.firstOrNull()?.set?.let {
                                            Text(
                                                "${it.reps} x ${weightFormat(it.weight)}",
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        }
                                    }
                                }
                            }
                            Space()

                            when {
                                pagerState.currentPage == pagerState.pageCount - 1 && pagerState.pageCount > 1 -> {
                                    FooterButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(0)
                                            }
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = stringResource(
                                                    Res.string.workout_screen_again_content_description
                                                )
                                            )
                                        }
                                    ) {
                                        val first = exercise.sections.first()
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End,
                                        ) {
                                            first.sets.lastOrNull()?.set?.let {
                                                Text(
                                                    "${it.reps} x ${weightFormat(it.weight)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }
                                }

                                exercise.sections.getOrNull(page + 1) != null -> {
                                    val next = exercise.sections[page + 1]
                                    FooterButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = stringResource(
                                                    Res.string.workout_screen_next_content_description
                                                )
                                            )
                                        },
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                        ) {
                                            next.sets.lastOrNull()?.set?.let {
                                                Text(
                                                    "${it.reps} x ${weightFormat(it.weight)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                )
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = {
                            showVariationDialog =
                                VariationDialogReason.Superset(exercise = exercise)
                        },
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text(stringResource(Res.string.workout_screen_superset_cta))
                    }
                }
            }
        }
    }

    if (showVariationDialog != null) {
        VariationSearchDialog(
            visible = showVariationDialog != null,
            textFieldPlaceholder = stringResource(Res.string.workout_add_exercise_cta),
            onDismissRequest = {
                showVariationDialog = null
            },
            onVariationSelected = {
                when (val reason = showVariationDialog) {
                    VariationDialogReason.AddExercise ->
                        eventHandler(CreateWorkoutEvent.AddExercise(it))

                    is VariationDialogReason.Superset ->
                        eventHandler(
                            CreateWorkoutEvent.AddSuperSet(
                                exercise = reason.exercise,
                                it
                            )
                        )

                    null -> {}
                }
                showVariationDialog = null
            }
        )
    }
}

@Composable
private fun FooterButton(
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                it()
                Space(MaterialTheme.spacing.half)
            }
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                content()
            }
            trailingIcon?.let {
                Space(MaterialTheme.spacing.half)
                it()
            }
        }
    }
}

private fun Modifier.variationCardAnimation(pagerState: PagerState, page: Int) = this
    .zIndex(if (page == pagerState.currentPage) 1f else 0f)
    .graphicsLayer {
        val pageOffset = (
            (pagerState.currentPage - page) + pagerState
                .currentPageOffsetFraction
            ).absoluteValue

        alpha = lerp(
            start = 0.5f,
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        )
        scaleY = lerp(
            start = 0.8f,
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        )
        scaleX = lerp(
            start = 0.8f,
            stop = 1f,
            fraction = 1f - pageOffset.coerceIn(0f, 1f)
        )
    }

@Composable
fun WorkoutSectionCard(
    modifier: Modifier,
    section: ExerciseSectionItem,
    eventHandler: (CreateWorkoutEvent) -> Unit,
    date: LocalDate,
    index: Int? = null,
    footer: @Composable () -> Unit = {},
) {
    val sectionSets = section.sets
    val movements = sectionSets.map { it.movement }
    val movementNames = movements.distinctBy { it.name }.joinToString(", ") { it.name ?: "" }

    Card(
        modifier = modifier,
    ) {
        Column {
            Column(
                modifier = Modifier.wrapContentHeight().fillMaxWidth()
            ) {
                val coordinator = LocalNavCoordinator.current
                Column(
                    modifier = Modifier
                        .padding(
                            top = MaterialTheme.spacing.threeQuarters,
                            start = MaterialTheme.spacing.one,
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val prefix = index?.let { 'A'.plus(index).plus(".") } ?: ""
                        Text(
                            "$prefix $movementNames".trim(),
                            style = MaterialTheme.typography.titleLarge,
                        )

                        Space()

                        var showWarning by remember { mutableStateOf(false) }

                        if (showWarning) {
                            WarningDialog(
                                text = stringResource(Res.string.workout_screen_delete_warning_text),
                                onDismiss = { showWarning = false },
                                onConfirm = {
                                    eventHandler(
                                        CreateWorkoutEvent.DeleteExerciseSection(
                                            section
                                        )
                                    )
                                    showWarning = false
                                }
                            )
                        }

                        IconButton(
                            onClick = {
                                showWarning = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(
                                    Res.string.workout_screen_delete_variation_content_description,
                                    movementNames,
                                )
                            )
                        }
                    }

                    sectionSets.maxByOrNull { it.set.weight }?.movement?.let {
                        Text(
                            it.maxText(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    if (LocalTwmSettings.current) {
                        Text(
                            "Total Weight Moved: ${
                                "${
                                    sectionSets.map { it.set }.sumOf { it.reps * it.weight }
                                        .decimalFormat()
                                } ${LocalUnitOfMeasure.current.value}"
                            }",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            val copyIndex = remember(section.sets) { section.sets.indexOfLast { !it.recommended } }
            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.small,
                )
            ) {
                section.sets.forEachIndexed { index, sectionSet ->
                    val set = sectionSet.set
                    var showOptionsDialog by remember { mutableStateOf(false) }
                    var visibility by remember { mutableStateOf<Boolean?>(null) }

                    if (showOptionsDialog) {
                        SetOptionsBottomSheet(
                            onDeleteRequest = {
                                visibility = false
                                showOptionsDialog = false
                            },
                            onDuplicateRequest = {
                                eventHandler(CreateWorkoutEvent.DuplicateSet(set))
                                showOptionsDialog = false
                            },
                            onDismissRequest = {
                                showOptionsDialog = false
                            }
                        )
                    }

                    val coordinator = LocalNavCoordinator.current
                    AnimatedVisibility(
                        visible = visibility ?: false
                    ) {
                        SetInfoRow(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 52.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (sectionSet.recommended) {
                                            coordinator.present(
                                                Destination.EditSet(setId = set.id)
                                            )
                                        } else {
                                            eventHandler(CreateWorkoutEvent.DuplicateSet(set, true))
                                        }
                                    },
                                    onLongClick = {
                                        showOptionsDialog = true
                                    },
                                    role = Role.Button
                                )
                                .border(
                                    color = when {
                                        index == copyIndex -> MaterialTheme.colorScheme.onSurface
                                        sectionSet.recommended -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                    },
                                    width = 1.dp,
                                    shape = MaterialTheme.shapes.small.copy(
                                        topStart = if (index == 0) {
                                            MaterialTheme.shapes.small.topStart
                                        } else {
                                            CornerSize(0.dp)
                                        },
                                        topEnd = if (index == 0) {
                                            MaterialTheme.shapes.small.topStart
                                        } else {
                                            CornerSize(0.dp)
                                        }
                                    )
                                )
                                .padding(
                                    horizontal = MaterialTheme.spacing.one,
                                    vertical = MaterialTheme.spacing.half
                                ),
                            set = set
                        )
                    }

                    LaunchedEffect(visibility) {
                        if (visibility == false) {
                            eventHandler(CreateWorkoutEvent.DeleteSet(set))
                        } else if (visibility == null) {
                            visibility = true
                        }
                    }
                }
            }

            if (section.sets.isEmpty()) {
                val coordinator = LocalNavCoordinator.current
                IconButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        coordinator.present(
                            Destination.CreateSet(
                                date = date.atStartOfDayIn(TimeZone.currentSystemDefault()),
                                sectionId = section.id
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.workout_screen_add_set_content_description)
                    )
                }
            } else {
                if (copyIndex != -1) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            eventHandler(CreateWorkoutEvent.DuplicateSet(section.sets[copyIndex].set))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(
                                Res.string.workout_screen_duplicate_last_set_content_description
                            )
                        )
                    }
                }
            }
            footer()
        }
    }
}

@Composable
fun SetOptionsBottomSheet(
    onDeleteRequest: () -> Unit,
    onDuplicateRequest: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column {
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = Dp.AccessibilityMinimumSize)
                    .clickable(
                        onClick = onDeleteRequest,
                        role = Role.Button
                    ).padding(
                        horizontal = MaterialTheme.spacing.one,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.workout_set_options_delete_cta)
                )
                Space(MaterialTheme.spacing.half)
                Text(stringResource(Res.string.workout_set_options_delete_cta))
            }
            Row(
                modifier = Modifier
                    .defaultMinSize(minHeight = Dp.AccessibilityMinimumSize)
                    .clickable(
                        onClick = onDuplicateRequest,
                        role = Role.Button
                    ).padding(
                        horizontal = MaterialTheme.spacing.one,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(Res.string.workout_set_options_copy_cta)
                )
                Space(MaterialTheme.spacing.half)
                Text(stringResource(Res.string.workout_set_options_copy_cta))
            }
        }
    }
}

@ThemePreviews
@Composable
fun WorkoutScreenInternalPreview(
    @PreviewParameter(WorkoutStateProvider::class) state: CreateWorkoutState,
) {
    PreviewAppTheme(isDarkMode = isSystemInDarkTheme()) {
        WorkoutScreenInternal(
            state = state,
            eventHandler = {}
        )
    }
}

class WorkoutStateProvider: PreviewParameterProvider<CreateWorkoutState> {
    override val values: Sequence<CreateWorkoutState>
        get() = sequenceOf(
            // Empty workout with no exercises
            CreateWorkoutState(
                date = LocalDate(2024, 1, 15),
                notes = "",
                warmup = null,
                finisher = null,
                exercises = emptyList(),
                recentWorkouts = emptyList()
            ),
            // Workout with notes and warmup/finisher
            CreateWorkoutState(
                date = LocalDate(2024, 1, 15),
                notes = "Feeling strong today! PRs coming!",
                warmup = "5 min cardio, dynamic stretches",
                finisher = "Core: 3x20 ab wheel, 3x60s plank",
                exercises = emptyList(),
                recentWorkouts = emptyList()
            ),
            // Empty workout with recent workouts to copy
            CreateWorkoutState(
                date = LocalDate(2024, 1, 15),
                notes = "",
                exercises = emptyList(),
                recentWorkouts = listOf(
                    Workout(
                        id = "w1",
                        date = LocalDate(2024, 1, 12),
                        exercises = listOf(
                            com.lift.bro.domain.models.Exercise(
                                id = "ex1",
                                workoutId = "w1",
                                sections = listOf(
                                    Section(
                                        id = "vs1",
                                        exerciseId = "",
                                        recommendedSets = emptyList(),
                                        sets = listOf(
                                            LBSet(
                                                id = "set1",
                                                variationId = "vs1",
                                            )
                                        )
                                    ),
                                    Section(
                                        id = "vs1",
                                        exerciseId = "",
                                        recommendedSets = emptyList(),
                                        sets = listOf(
                                            LBSet(
                                                id = "set1",
                                                variationId = "vs1",
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    Workout(
                        id = "w2",
                        date = LocalDate(2024, 1, 10),
                        exercises = listOf(
                            com.lift.bro.domain.models.Exercise(
                                id = "ex2",
                                workoutId = "w2",
                                sections = emptyList()
                            )
                        )
                    )
                )
            ),
            // Workout with exercises and sets
            CreateWorkoutState(
                date = LocalDate(2024, 1, 15),
                notes = "Heavy squat day",
                warmup = "10 min warmup",
                exercises = listOf(
                    ExerciseItem(
                        id = "ex1",
                        sections = listOf(
                            ExerciseSectionItem(
                                id = "var1",
                                sets = listOf(
                                    ExerciseSectionSet(
                                        movement = com.lift.bro.domain.models.Movement(
                                            lift = com.lift.bro.domain.models.Category(
                                                name = "Squat",
                                                color = 0xFF2196F3uL
                                            ),
                                            name = "Back Squat"
                                        ),
                                        set = LBSet(
                                            id = "set1",
                                            variationId = "var1",
                                            weight = 225.0,
                                            reps = 5,
                                            rpe = 7,
                                            date = kotlin.time.Clock.System.now()
                                        ),
                                        recommended = false,
                                    ),
                                    ExerciseSectionSet(
                                        movement = com.lift.bro.domain.models.Movement(
                                            lift = com.lift.bro.domain.models.Category(
                                                name = "Squat",
                                                color = 0xFF2196F3uL
                                            ),
                                            name = "Back Squat"
                                        ),
                                        set = LBSet(
                                            id = "set1",
                                            variationId = "var1",
                                            weight = 225.0,
                                            reps = 5,
                                            rpe = 7,
                                            date = kotlin.time.Clock.System.now()
                                        ),
                                        recommended = false,
                                    ),
                                )
                            )
                        )
                    )
                ),
                recentWorkouts = emptyList()
            )
        )
}
