package com.lift.bro.presentation.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.models.fullName
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.prettyPrintSet
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.excercise_screen_duplicate_cta
import lift_bro.core.generated.resources.excercise_screen_new_set_cta
import lift_bro.core.generated.resources.excercise_screen_title
import lift_bro.core.generated.resources.excercise_string_title_date_format
import org.jetbrains.compose.resources.stringResource

@Composable
fun WorkoutDetailsScreen(
    date: LocalDate,
) {
    val workout by dependencies.database.setDataSource.listenAll()
        .map { it.filter { it.date.toLocalDate() == date } }
        .map {
            it.groupBy { it.variationId }
                .map {
                    dependencies.database.variantDataSource.get(it.key) to it.value
                }
                .map {
                    Exercise(
                        sets = it.second,
                        variation = it.first!!,
                    )
                }
        }
        .map {
            Workout(
                date = date,
                warmup = "",
                exercises = it,
                finisher = "",
            )
        }
        .collectAsState(null)

    workout?.let {
        WorkoutDetailsScreen(
            workout = it
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WorkoutDetailsScreen(
    workout: Workout,
) {
    val subscriptionType by LocalSubscriptionStatusProvider.current
    val showTwm by dependencies.settingsRepository.shouldShowTotalWeightMoved()
        .map { it && subscriptionType == SubscriptionType.Pro }
        .collectAsState(false)

    val exercises = workout.exercises

    LiftingScaffold(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(Res.string.excercise_screen_title))

                Text(
                    workout.date.toString(stringResource(Res.string.excercise_string_title_date_format)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth().animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            items(exercises) { exercise ->
                Card {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        val coordinator = LocalNavCoordinator.current
                        Column(
                            modifier = Modifier.clickable(
                                onClick = {
                                    coordinator.present(Destination.VariationDetails(exercise.variation.id))
                                }
                            ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                exercise.variation.fullName,
                                style = MaterialTheme.typography.titleLarge,
                            )

                            if (showTwm) {
                                Text(
                                    "Total Weight Moved: ${"${exercise.totalWeightMoved.decimalFormat()} ${LocalUnitOfMeasure.current.value}"}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }

                            exercise.variation.notes?.let {
                                Text(
                                    text = exercise.variation.notes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
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

                        Button(
                            colors = ButtonDefaults.outlinedButtonColors(),
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

            item {
                val coordinator = LocalNavCoordinator.current
                Button(
                    onClick = {
                        coordinator.present(
                            Destination.EditSet()
                        )
                    }
                ) {
                    Text(stringResource(Res.string.excercise_screen_new_set_cta))
                }
            }
        }
    }
}

@Composable
fun SetInfoRow(
    modifier: Modifier = Modifier,
    set: LBSet,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = set.prettyPrintSet(),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        set.tempo.render()
        if (set.notes.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = MaterialTheme.spacing.quarter),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(MaterialTheme.typography.labelSmall.fontSize.value.dp),
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                )
                Space(MaterialTheme.spacing.quarter)
                Text(
                    text = set.notes,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
