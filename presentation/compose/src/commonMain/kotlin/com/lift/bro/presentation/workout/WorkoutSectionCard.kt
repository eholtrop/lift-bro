package com.lift.bro.presentation.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.LocalEMaxSettings
import com.lift.bro.presentation.LocalNavCoordinator
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.category.WarningDialog
import com.lift.bro.ui.Card
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.ThemePreviews
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.maxText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.workout_section_card_item_cta
import lift_bro.core.generated.resources.workout_section_card_primary_cta
import lift_bro.core.generated.resources.workout_section_card_secondary_cta
import lift_bro.core.generated.resources.workout_section_card_warning_dialog_title
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.padding.vertical.padding

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

    Card(
        modifier = modifier,
    ) {
        Column {
            Column(
                modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(
                                top = MaterialTheme.spacing.threeQuarters,
                                start = MaterialTheme.spacing.one,
                            ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val prefix = index?.let { 'A'.plus(index).plus(".") } ?: ""
                        Text(
                            "$prefix ${section.primaryMovement?.name}".trim(),
                            style = MaterialTheme.typography.titleLarge,
                        )

                        Space()

                        var showWarning by remember { mutableStateOf(false) }

                        if (showWarning) {
                            WarningDialog(
                                text = stringResource(Res.string.workout_section_card_warning_dialog_title),
                                onDismiss = { showWarning = false },
                                onConfirm = {
                                    eventHandler(
                                        CreateWorkoutEvent.DeleteExerciseSection(
                                            section,
                                        ),
                                    )
                                    showWarning = false
                                },
                            )
                        }

                        IconButton(
                            onClick = {
                                showWarning = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription =
                                    stringResource(
                                        Res.string.workout_section_card_secondary_cta,
                                        "$prefix Section ${section.primaryMovement?.name}".trim(),
                                    ),
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
                modifier =
                    Modifier
                        .padding(
                            horizontal = MaterialTheme.spacing.quarter,
                            top = MaterialTheme.spacing.half,
                        ).background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.small,
                        ),
            ) {
                when (section.sets.isEmpty()) {
                    true -> {
                        section.primaryMovement?.let { pm ->
                            pm.latestSet?.let {
                                SetInfoRow(
                                    modifier =
                                        Modifier.padding(
                                            horizontal = MaterialTheme.spacing.one,
                                            vertical = MaterialTheme.spacing.half,
                                        ),
                                    set = it,
                                )
                            }
                            pm.maxReps?.let {
                                SetInfoRow(
                                    set = it,
                                )
                            }
                            if (LocalEMaxSettings.current) {
                                pm.eMax?.let {
                                    SetInfoRow(
                                        set = it,
                                    )
                                }
                            }
                        }
                    }

                    false -> {
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
                                    },
                                )
                            }

                            val coordinator = LocalNavCoordinator.current
                            AnimatedVisibility(
                                visible = visibility ?: false,
                            ) {
                                SetInfoRow(
                                    modifier =
                                        Modifier
                                            .defaultMinSize(minHeight = 52.dp)
                                            .combinedClickable(
                                                onClick = {
                                                    when (sectionSet.recommended) {
                                                        true -> eventHandler(CreateWorkoutEvent.DuplicateSet(set, true))
                                                        false -> coordinator.present(Destination.EditSet(setId = set.id))
                                                    }
                                                },
                                                onLongClick = {
                                                    showOptionsDialog = true
                                                },
                                                role = Role.Button,
                                            ).border(
                                                color =
                                                    when {
                                                        index == copyIndex -> MaterialTheme.colorScheme.onSurface
                                                        sectionSet.recommended -> MaterialTheme.colorScheme.secondary
                                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                                    },
                                                width = 1.dp,
                                                shape =
                                                    MaterialTheme.shapes.small.copy(
                                                        topStart =
                                                            if (index == 0) {
                                                                MaterialTheme.shapes.small.topStart
                                                            } else {
                                                                CornerSize(0.dp)
                                                            },
                                                        topEnd =
                                                            if (index == 0) {
                                                                MaterialTheme.shapes.small.topStart
                                                            } else {
                                                                CornerSize(0.dp)
                                                            },
                                                    ),
                                            ).padding(
                                                horizontal = MaterialTheme.spacing.one,
                                                vertical = MaterialTheme.spacing.half,
                                            ),
                                    set = set,
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
                                movementId = section.primaryMovement?.id,
                                sectionId = section.id,
                            ),
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.workout_section_card_primary_cta),
                    )
                }
            } else {
                if (copyIndex != -1) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            eventHandler(CreateWorkoutEvent.DuplicateSet(section.sets[copyIndex].set))
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription =
                                stringResource(
                                    Res.string.workout_section_card_item_cta,
                                ),
                        )
                    }
                }
            }
            footer()
        }
    }
}

@ThemePreviews
@Composable
fun WorkoutSectionCardPreview(
    @PreviewParameter(SectionItemProvider::class) section: ExerciseSectionItem,
) {
    PreviewAppTheme(isDarkMode = isSystemInDarkTheme()) {
        WorkoutSectionCard(
            modifier = Modifier.fillMaxWidth(),
            section = section,
            eventHandler = {},
            date = LocalDate(2024, 1, 15),
        )
    }
}

class SectionItemProvider : PreviewParameterProvider<ExerciseSectionItem> {
    override val values: Sequence<ExerciseSectionItem>
        get() =
            sequenceOf(
                ExerciseSectionItem(
                    id = "section1",
                    primaryMovement =
                        Movement(
                            id = "mov1",
                            name = "Bench Press",
                            latestSet =
                                LBSet(
                                    id = "latest1",
                                    movementId = "mov1",
                                    weight = 185.0,
                                    reps = 8,
                                ),
                            eMax =
                                LBSet(
                                    id = "emax1",
                                    movementId = "mov1",
                                    weight = 175.0,
                                    reps = 10,
                                ),
                            maxReps =
                                LBSet(
                                    id = "maxreps1",
                                    movementId = "mov1",
                                    weight = 135.0,
                                    reps = 20,
                                ),
                        ),
                    sets =
                        listOf(
                            ExerciseSectionSet(
                                set =
                                    LBSet(
                                        id = "set1",
                                        movementId = "mov1",
                                        weight = 225.0,
                                        reps = 5,
                                        rpe = 7,
                                        tempo = Tempo(down = 3, hold = 1, up = 1),
                                    ),
                                movement = Movement(id = "mov1", name = "Bench Press"),
                                recommended = false,
                            ),
                            ExerciseSectionSet(
                                set =
                                    LBSet(
                                        id = "set2",
                                        movementId = "mov1",
                                        weight = 205.0,
                                        reps = 8,
                                        rpe = 8,
                                        tempo = Tempo(down = 3, hold = 1, up = 1),
                                    ),
                                movement = Movement(id = "mov1", name = "Bench Press"),
                                recommended = false,
                            ),
                            ExerciseSectionSet(
                                set =
                                    LBSet(
                                        id = "set3",
                                        movementId = "mov1",
                                        weight = 185.0,
                                        reps = 10,
                                        rpe = 9,
                                        tempo = Tempo(down = 3, hold = 1, up = 1),
                                    ),
                                movement = Movement(id = "mov1", name = "Bench Press"),
                                recommended = false,
                            ),
                            ExerciseSectionSet(
                                set =
                                    LBSet(
                                        id = "rec1",
                                        movementId = "mov1",
                                        weight = 205.0,
                                        reps = 8,
                                        tempo = Tempo(down = 3, hold = 1, up = 1),
                                    ),
                                movement = Movement(id = "mov1", name = "Bench Press"),
                                recommended = true,
                            ),
                        ),
                ),
                ExerciseSectionItem(
                    id = "section2",
                    primaryMovement =
                        Movement(
                            id = "mov2",
                            name = "Squat",
                            latestSet =
                                LBSet(
                                    id = "latest2",
                                    movementId = "mov2",
                                    weight = 315.0,
                                    reps = 3,
                                ),
                            eMax =
                                LBSet(
                                    id = "emax2",
                                    movementId = "mov2",
                                    weight = 275.0,
                                    reps = 10,
                                ),
                            maxReps =
                                LBSet(
                                    id = "maxreps2",
                                    movementId = "mov2",
                                    weight = 225.0,
                                    reps = 15,
                                ),
                        ),
                    sets = emptyList(),
                ),
            )
}
