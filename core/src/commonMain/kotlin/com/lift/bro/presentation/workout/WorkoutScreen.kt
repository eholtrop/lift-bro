@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import app.cash.sqldelight.ColumnAdapter
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.VariationSets
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.maxText
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.lift.WarningDialog
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.formattedWeight
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.lift.bro.utils.toString
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.workout_variation_card_primary_cta
import lift_bro.core.generated.resources.workout_screen_title
import lift_bro.core.generated.resources.workout_notes_placeholder
import lift_bro.core.generated.resources.workout_warmup_label
import lift_bro.core.generated.resources.workout_warmup_placeholder
import lift_bro.core.generated.resources.workout_add_warmup_cta
import lift_bro.core.generated.resources.workout_finisher_label
import lift_bro.core.generated.resources.workout_finisher_placeholder
import lift_bro.core.generated.resources.workout_add_finisher_cta
import lift_bro.core.generated.resources.workout_add_exercise_cta
import lift_bro.core.generated.resources.workout_set_options_copy_cta
import lift_bro.core.generated.resources.workout_set_options_delete_cta
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue


@Composable
fun CreateWorkoutScreen(
    interactor: Interactor<CreateWorkoutState, CreateWorkoutEvent>,
) {
    val state by interactor.state.collectAsState()

    CreateWorkoutScreenInternal(
        state = state,
        eventHandler = { interactor(it) }
    )
}

sealed class VariationDialogReason {
    object AddExercise: VariationDialogReason()
    data class Superset(val exercise: Exercise): VariationDialogReason()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreenInternal(
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

            items(state.exercises) { exercise ->
                val pagerState = rememberPagerState(pageCount = { exercise.variationSets.size })
                val coroutineScope = rememberCoroutineScope()
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one),
                    pageSpacing = MaterialTheme.spacing.two.times(-2),
                ) { page ->
                    val vSets = exercise.variationSets[page]
                    VariationSetCard(
                        modifier = Modifier.animateContentSize()
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
                            },
                        variationSet = vSets,
                        eventHandler = eventHandler
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            exercise.variationSets.getOrNull(page - 1)?.let { previous ->
                                FooterButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ChevronLeft,
                                            contentDescription = "Previous Lift"
                                        )

                                    }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        previous.sets.firstOrNull()?.let {
                                            Text(
                                                "${it.reps} x ${weightFormat(it.weight)}",
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        }
                                        Text(
                                            previous.variation.fullName,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
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
                                                contentDescription = "Again"
                                            )
                                        }
                                    ) {
                                        val first = exercise.variationSets.first()
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End,
                                        ) {
                                            first.sets.lastOrNull()?.let {
                                                Text(
                                                    "${it.reps} x ${weightFormat(it.weight)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    maxLines = 1,
                                                )
                                            }
                                            Text(
                                                first.variation.fullName,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
                                    }
                                }

                                exercise.variationSets.getOrNull(page + 1) != null -> {
                                    val next = exercise.variationSets[page + 1]
                                    FooterButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "Next"
                                            )
                                        },
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                        ) {
                                            next.sets.lastOrNull()?.let {
                                                Text(
                                                    "${it.reps} x ${weightFormat(it.weight)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                )
                                            }
                                            Text(
                                                next.variation.fullName,
                                                style = MaterialTheme.typography.labelSmall,
                                            )
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
                        Text("Superset with...")
                    }
                }
            }
        }
    }

    VariationSearchDialog(
        visible = showVariationDialog != null,
        textFieldPlaceholder = stringResource(Res.string.workout_add_exercise_cta),
        onDismissRequest = {
            showVariationDialog = null
        },
        onVariationSelected = {
            when (showVariationDialog) {
                VariationDialogReason.AddExercise ->
                    eventHandler(CreateWorkoutEvent.AddExercise(it))

                is VariationDialogReason.Superset ->
                    eventHandler(
                        CreateWorkoutEvent.AddSuperSet(
                            exercise = (showVariationDialog as VariationDialogReason.Superset).exercise,
                            it
                        )
                    )

                null -> {}
            }
            showVariationDialog = null
        }
    )
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
            content()
            trailingIcon?.let {
                Space(MaterialTheme.spacing.half)
                it()
            }
        }
    }
}

@Composable
fun VariationSetCard(
    modifier: Modifier,
    variationSet: VariationSets,
    eventHandler: (CreateWorkoutEvent) -> Unit,
    footer: @Composable () -> Unit = {},
) {
    val variation = variationSet.variation
    val sets = variationSet.sets

    Card(
        modifier = modifier.animateContentSize(),
    ) {
        Column {
            Column(
                modifier = Modifier.wrapContentHeight().fillMaxWidth()
            ) {
                val coordinator = LocalNavCoordinator.current
                Column(
                    modifier = Modifier.clickable(
                        onClick = {
                            coordinator.present(
                                Destination.VariationDetails(
                                    variation.id
                                )
                            )
                        }
                    )
                        .padding(
                            top = MaterialTheme.spacing.threeQuarters,
                            start = MaterialTheme.spacing.one,
                        ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            variation.fullName,
                            style = MaterialTheme.typography.titleLarge,
                        )

                        Space()

                        var showWarning by remember { mutableStateOf(false) }

                        if (showWarning) {
                            WarningDialog(
                                text = "This will delete all in this workout, are you sure?",
                                onDismiss = { showWarning = false },
                                onConfirm = {
                                    eventHandler(
                                        CreateWorkoutEvent.DeleteVariation(
                                            variationSet
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
                                contentDescription = "Delete ${variation.fullName}"
                            )
                        }
                    }

                    Text(
                        variation.maxText(),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    if (LocalTwmSettings.current) {
                        Text(
                            "Total Weight Moved: ${
                                "${
                                    variationSet.sets.sumOf { it.reps * it.weight }.decimalFormat()
                                } ${LocalUnitOfMeasure.current.value}"
                            }",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    variation.notes?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            sets.forEachIndexed { index, set ->

                var showOptionsDialog by remember { mutableStateOf(false) }

                if (showOptionsDialog) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showOptionsDialog = false
                        }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = Dp.AccessibilityMinimumSize)
                                    .clickable(
                                        onClick = {
                                            eventHandler(
                                                CreateWorkoutEvent.DeleteSet(
                                                    set
                                                )
                                            )
                                            showOptionsDialog = false
                                        },
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
                                        onClick = {
                                            eventHandler(
                                                CreateWorkoutEvent.DuplicateSet(
                                                    set
                                                )
                                            )
                                            showOptionsDialog = false
                                        },
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

                val coordinator = LocalNavCoordinator.current
                SetInfoRow(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 52.dp)
                        .combinedClickable(
                            onClick = {
                                coordinator.present(
                                    Destination.EditSet(
                                        setId = set.id
                                    )
                                )
                            },
                            onLongClick = {
                                showOptionsDialog = true
                            },
                            role = Role.Button
                        )
                        .padding(
                            horizontal = MaterialTheme.spacing.one,
                            vertical = MaterialTheme.spacing.half
                        ),
                    set = set
                )
            }

            if (sets.isEmpty()) {
                val coordinator = LocalNavCoordinator.current
                IconButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        coordinator.present(
                            Destination.CreateSet(
                                variationId = variation.id,
                                date = sets.firstOrNull()?.date ?: Clock.System.now(),
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Set"
                    )
                }
            } else {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        eventHandler(CreateWorkoutEvent.DuplicateSet(sets.last()))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Last Set"
                    )
                }
            }
            footer()
        }
    }
}