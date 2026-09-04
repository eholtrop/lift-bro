package com.lift.bro.presentation.workout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.spatial.RelativeLayoutBounds
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.RecommendedSet
import com.lift.bro.domain.models.SectionSet
import com.lift.bro.domain.models.SetTarget
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.LocalEMaxSettings
import com.lift.bro.presentation.LocalNavCoordinator
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.category.WarningDialog
import com.lift.bro.presentation.movement.render
import com.lift.bro.presentation.set.RepWeightSelector
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteExerciseSection
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DuplicateSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.PerformSet
import com.lift.bro.ui.Card
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.card.lift.weightFormat
import com.lift.bro.ui.navigation.Destination.CreateSet
import com.lift.bro.ui.navigation.Destination.EditSet
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.ThemePreviews
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.maxText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.workout_section_card_primary_cta
import lift_bro.core.generated.resources.workout_section_card_secondary_cta
import lift_bro.core.generated.resources.workout_section_card_warning_dialog_title
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.AccessibilityMinimumSize
import tv.dpal.compose.listCorners
import tv.dpal.compose.padding.horizontal.padding
import tv.dpal.compose.padding.vertical.padding
import tv.dpal.ext.ktx.datetime.toString
import kotlin.math.roundToInt
import kotlin.time.Instant

private data class AnchorRect(val offset: Offset, val size: Size)

@Composable
fun WorkoutSectionCard(
    modifier: Modifier,
    section: ExerciseSectionItem,
    eventHandler: (CreateWorkoutEvent) -> Unit,
    date: LocalDate,
    index: Int? = null,
    footer: @Composable () -> Unit = {},
) {
    val coordinator = LocalNavCoordinator.current
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
                                        DeleteExerciseSection(
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

                    section.primaryMovement?.let {
                        Text(
                            it.maxText(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    if (LocalTwmSettings.current) {
                        Text(
                            "Total Weight Moved: ${
                                "${section.twm.decimalFormat()} ${LocalUnitOfMeasure.current.value}"
                            }",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            val copyIndex = remember(section.sets) { section.sets.indexOfLast { it is SectionSet.Performed } }
            val previousSetColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f)

            var containerCoordinates by remember { mutableStateOf<RelativeLayoutBounds?>(null) }
            var currentAnchor by remember { mutableStateOf<AnchorRect?>(null) }

            val windowPosition = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
            val windowSize = remember { Animatable(Size.Zero, Size.VectorConverter) }
            val windowAlpha = remember { Animatable(0f) }

            LaunchedEffect(currentAnchor == null) {
                windowAlpha.animateTo(
                    targetValue = if (currentAnchor == null) 0f else 1f,
                    animationSpec = tween(durationMillis = 400),
                )
            }

            LaunchedEffect(currentAnchor) {
                val target = currentAnchor ?: return@LaunchedEffect
                windowPosition.snapTo(target.offset)
                windowSize.snapTo(target.size)
            }

            Box(
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.quarter,
                    top = MaterialTheme.spacing.half,
                ).background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.small,
                ).border(
                    color = previousSetColor,
                    width = 2.dp,
                    shape = MaterialTheme.shapes.small,
                ).clip(MaterialTheme.shapes.small),
            ) {
                Column(
                    modifier = Modifier
                        .onLayoutRectChanged(debounceMillis = 1) {
                            containerCoordinates = it
                        }
                        .animateContentSize()
                ) {
                    when (section.sets.isEmpty()) {
                        true -> {
                            section.primaryMovement?.let { pm ->
                                val title: @Composable (String, Instant) -> AnnotatedString = { title, date ->
                                    buildAnnotatedString {
                                        append(title)

                                        withStyle(
                                            MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ).toSpanStyle(),
                                        ) {
                                            append(" (${date.toString("MMM d")})")
                                        }
                                    }
                                }

                                val modifier: (LBSet) -> Modifier = { set ->
                                    Modifier.clickable(
                                        onClick = {
                                            coordinator.present(
                                                EditSet(setId = set.id)
                                            )
                                        },
                                        onClickLabel = "Open"
                                    ).border(
                                        color = previousSetColor,
                                        width = 2.dp,
                                    ).padding(
                                        horizontal = MaterialTheme.spacing.one,
                                        vertical = MaterialTheme.spacing.one,
                                    )
                                }

                                CompositionLocalProvider(
                                    value = LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = .6f
                                    )
                                ) {
                                    pm.latestSet?.let {
                                        SetInfoRow(
                                            modifier = modifier(it),
                                            label = {
                                                Text(title("Latest Set", it.date))
                                            },
                                            set = it,
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    eventHandler(
                                                        DuplicateSet(it, true, sectionId = section.id)
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy"
                                                )
                                            }
                                        }
                                    }
                                    if (LocalEMaxSettings.current) {
                                        pm.eMax?.let {
                                            SetInfoRow(
                                                modifier = modifier(it),
                                                label = {
                                                    Text(title("eMax Rep", it.date))
                                                },
                                                set = it,
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        eventHandler(
                                                            DuplicateSet(
                                                                it,
                                                                true,
                                                                sectionId = section.id
                                                            )
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (LocalTwmSettings.current) {
                                        pm.eMax?.let {
                                            SetInfoRow(
                                                modifier = modifier(it),
                                                label = {
                                                    Text(title("Most Weight Moved", it.date))
                                                },
                                                set = it,
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        eventHandler(
                                                            DuplicateSet(
                                                                it,
                                                                true,
                                                                sectionId = section.id
                                                            )
                                                        )
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        false -> {
                            section.sets.forEachIndexed { index, sectionSet ->
                                var showOptionsDialog by remember(index) { mutableStateOf(false) }
                                var visibility by rememberSaveable { mutableStateOf<Boolean?>(null) }

                                if (showOptionsDialog) {
                                    SetOptionsBottomSheet(
                                        onDeleteRequest = {
                                            visibility = false
                                            showOptionsDialog = false
                                        },
                                        onDuplicateRequest = {
                                            when (sectionSet) {
                                                is WorkoutSet.Performed -> {
                                                    eventHandler(DuplicateSet(sectionSet.set))
                                                    showOptionsDialog = false
                                                }

                                                is WorkoutSet.Recommended -> {
                                                }

                                                is WorkoutSet.Current -> {
                                                    eventHandler(PerformSet(sectionSet))
                                                }
                                            }
                                        },
                                        onDismissRequest = {
                                            showOptionsDialog = false
                                        },
                                    )
                                }

                                AnimatedContent(
                                    targetState = sectionSet,
                                    modifier = Modifier
                                        .onLayoutRectChanged(
                                            debounceMillis = 1
                                        ) {
                                            if (sectionSet is WorkoutSet.Current) {
                                                currentAnchor = AnchorRect(
                                                    offset = it.positionInRoot.toOffset() - (containerCoordinates?.positionInRoot?.toOffset() ?: Offset.Zero),
                                                    size = it.boundsInRoot.size.toSize(),
                                                )
                                            }
                                        }
                                        .onGloballyPositioned {
                                            if (sectionSet is WorkoutSet.Current) {
                                                currentAnchor = AnchorRect(
                                                    offset = it.positionInRoot() - (containerCoordinates?.positionInRoot?.toOffset() ?: Offset.Zero),
                                                    size = it.size.toSize(),
                                                )
                                            }
                                        },
                                    transitionSpec = {
                                        (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
                                    },
                                ) { current ->
                                    when (current) {
                                        is WorkoutSet.Recommended -> {
                                            RecommendedSetRow(
                                                modifier = Modifier.border(
                                                    color = when {
                                                        index == copyIndex -> MaterialTheme.colorScheme.onSurface
                                                        else -> MaterialTheme.colorScheme.surfaceContainer
                                                    },
                                                    width = 1.dp,
                                                    shape = MaterialTheme.shapes.small.listCorners(index, sectionSets),
                                                ),
                                                recommendedSet = current.recommendedSet,
                                                section = section,
                                            )
                                        }

                                        is WorkoutSet.Performed -> {
                                            SetInfoRow(
                                                modifier = Modifier
                                                    .defaultMinSize(minHeight = 52.dp)
                                                    .combinedClickable(
                                                        onClick = {
                                                            coordinator.present(
                                                                EditSet(setId = current.set.id)
                                                            )
                                                        },
                                                        onLongClick = {
                                                            showOptionsDialog = true
                                                        },
                                                        role = Role.Button,
                                                    ).border(
                                                        color = when {
                                                            index == copyIndex -> MaterialTheme.colorScheme.onSurface
                                                            else -> MaterialTheme.colorScheme.surfaceContainer
                                                        },
                                                        width = 1.dp,
                                                        shape = MaterialTheme.shapes.small.listCorners(
                                                            index,
                                                            sectionSets
                                                        ),
                                                    ).padding(
                                                        horizontal = MaterialTheme.spacing.one,
                                                        vertical = MaterialTheme.spacing.half,
                                                    ),
                                                set = current.set
                                            )
                                        }

                                        is WorkoutSet.Current -> {
                                            DisposableEffect(Unit) {
                                                onDispose { currentAnchor = null }
                                            }
                                            CurrentSetRow(
                                                modifier = Modifier,
                                                set = current,
                                                onCheckClicked = { currentSet ->
                                                    eventHandler(
                                                        CreateWorkoutEvent.PerformSet(
                                                            set = currentSet,
                                                            sectionId = section.id,
                                                        )
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }

                                LaunchedEffect(visibility) {
                                    if (visibility == false) {
                                        when (sectionSet) {
                                            is WorkoutSet.Performed -> eventHandler(DeleteSet(sectionSet.set))
                                            is WorkoutSet.Recommended -> {}
                                            is WorkoutSet.Current -> {}
                                        }
                                    } else if (visibility == null) {
                                        visibility = true
                                    }
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = windowAlpha.value }
                        .offset {
                            IntOffset(
                                windowPosition.value.x.roundToInt(),
                                windowPosition.value.y.roundToInt(),
                            )
                        }
                        .size(
                            with(LocalDensity.current) { windowSize.value.width.toDp() },
                            with(LocalDensity.current) { windowSize.value.height.toDp() },
                        )
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = .08f),
                            shape = MaterialTheme.shapes.small,
                        )
                        .border(
                            color = MaterialTheme.colorScheme.primary,
                            width = 2.dp,
                            shape = MaterialTheme.shapes.small,
                        ),
                )
            }
            if (section.sets.isEmpty()) {
                val coordinator = LocalNavCoordinator.current
                IconButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        coordinator.present(
                            CreateSet(
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
            }
            footer()
        }
    }
}

@Composable
fun CurrentSetRow(
    modifier: Modifier = Modifier,
    set: WorkoutSet.Current,
    onCheckClicked: (WorkoutSet.Current) -> Unit,
) {
    var recommendedSet by remember { mutableStateOf(set) }

    Row(
        modifier = modifier.padding(
            start = MaterialTheme.spacing.one,
            end = MaterialTheme.spacing.half,
            vertical = MaterialTheme.spacing.threeQuarters
        ).fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                RepWeightSelector(
                    weight = recommendedSet.weight,
                    weightChanged = {
                        it?.let {
                            recommendedSet = recommendedSet.copy(weight = it)
                        }
                    },
                    reps = recommendedSet.reps,
                    repChanged = {
                        it?.let {
                            recommendedSet = recommendedSet.copy(reps = it)
                        }
                    },
                    rpe = recommendedSet.rpe,
                    rpeChanged = {
                        it?.let {
                            recommendedSet = recommendedSet.copy(rpe = it)
                        }
                    },
                    showRpe = true,
                    showInfo = false,
                )
            }
            Space(MaterialTheme.spacing.quarter)
            Row {
                Text(
                    "Tempo: ",
                    style = MaterialTheme.typography.labelLarge,
                )
                recommendedSet.tempo.render(
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Space()

        IconButton(
            onClick = { onCheckClicked(recommendedSet) }
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Crushed!",
            )
        }
    }
}

@Composable
fun RecommendedSetRow(
    modifier: Modifier = Modifier,
    recommendedSet: RecommendedSet,
    section: ExerciseSectionItem,
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.half
            )
            .defaultMinSize(minHeight = Dp.AccessibilityMinimumSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            value = LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(
                alpha = .6f
            )
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (recommendedSet.movement.name != section.primaryMovement?.name) {
                    Text(
                        text = recommendedSet.movement.name ?: ""
                    )
                }
                Text(
                    text = when (val target = recommendedSet.target) {
                        is SetTarget.PercentageMax -> {
                            "${target.reps} x ${(target.percentage * 100).toInt()}% (${
                                weightFormat(
                                    target.percentage * target.max
                                )
                            })"
                        }

                        is SetTarget.Reps -> {
                            "${target.reps} x bw"
                        }

                        is SetTarget.Weight -> {
                            "${target.reps} x ${weightFormat(target.weight)}"
                        }
                    }
                )

                recommendedSet.tempo.render()
            }
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

class SectionItemProvider: PreviewParameterProvider<ExerciseSectionItem> {
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
                        WorkoutSet.Performed(
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
                        ),
                        WorkoutSet.Performed(
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
                        ),
                        WorkoutSet.Performed(
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
                        ),
                        WorkoutSet.Recommended(
                            RecommendedSet(
                                target = SetTarget.Weight(weight = 205.0, reps = 8),
                                tempo = Tempo(down = 3, hold = 1, up = 1),
                                movement = Movement(id = "mov1", name = "Bench Press"),
                            ),
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
                ExerciseSectionItem(
                    id = "section3",
                    primaryMovement =
                    Movement(
                        id = "mov3",
                        name = "Deadlift",
                        latestSet =
                        LBSet(
                            id = "latest3",
                            movementId = "mov3",
                            weight = 405.0,
                            reps = 3,
                        ),
                        eMax =
                        LBSet(
                            id = "emax3",
                            movementId = "mov3",
                            weight = 385.0,
                            reps = 10,
                        ),
                        maxReps =
                        LBSet(
                            id = "maxreps3",
                            movementId = "mov3",
                            weight = 315.0,
                            reps = 12,
                        ),
                    ),
                    sets =
                    listOf(
                        WorkoutSet.Recommended(
                            RecommendedSet(
                                target = SetTarget.PercentageMax(percentage = 0.85f, reps = 3, max = 405.0),
                                tempo = Tempo(down = 3, hold = 1, up = 1),
                                movement = Movement(id = "mov3", name = "Deadlift"),
                            ),
                        ),
                        WorkoutSet.Recommended(
                            RecommendedSet(
                                target = SetTarget.PercentageMax(percentage = 0.75f, reps = 5, max = 405.0),
                                tempo = Tempo(down = 3, hold = 1, up = 1),
                                movement = Movement(id = "mov3", name = "Deadlift"),
                            ),
                        ),
                    ),
                ),
                ExerciseSectionItem(
                    id = "section4",
                    primaryMovement =
                    Movement(
                        id = "mov4",
                        name = "Overhead Press",
                        latestSet =
                        LBSet(
                            id = "latest4",
                            movementId = "mov4",
                            weight = 115.0,
                            reps = 8,
                        ),
                        eMax =
                        LBSet(
                            id = "emax4",
                            movementId = "mov4",
                            weight = 105.0,
                            reps = 10,
                        ),
                        maxReps =
                        LBSet(
                            id = "maxreps4",
                            movementId = "mov4",
                            weight = 95.0,
                            reps = 15,
                        ),
                    ),
                    sets =
                    listOf(
                        WorkoutSet.Recommended(
                            RecommendedSet(
                                target = SetTarget.Reps(reps = 10, addedWeight = 5.0),
                                tempo = Tempo(down = 3, hold = 1, up = 1),
                                movement = Movement(id = "mov4", name = "Overhead Press"),
                            ),
                        ),
                    ),
                ),
            )
}
