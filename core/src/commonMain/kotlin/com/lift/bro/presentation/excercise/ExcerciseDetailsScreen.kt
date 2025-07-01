package com.lift.bro.presentation.excercise

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import com.lift.bro.domain.models.Excercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.fullName
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
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
fun ExcerciseDetailsScreen(
    date: LocalDate,
    variationId: String,
) {
    val sets by dependencies.database.setDataSource.listenAllForVariation(variationId)
        .map { it.filter { it.date.toLocalDate() == date } }
        .map { it.sortedBy { it.date } }
        .collectAsState(emptyList())

    val variation = dependencies.database.variantDataSource.get(variationId)!!

    ExcerciseDetailsScreen(
        excercise = Excercise(
            sets = sets,
            date = date,
            variation = variation,
        )
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExcerciseDetailsScreen(
    excercise: Excercise,
) {
    LiftingScaffold(
        title = stringResource(Res.string.excercise_screen_title),
    ) { padding ->
        Card(
            modifier = Modifier.padding(padding)
                .padding(horizontal = MaterialTheme.spacing.one)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(excercise.variation.fullName)
                }

                item {
                    Text(excercise.date.toString(stringResource(Res.string.excercise_string_title_date_format)))
                }

                excercise.sets.forEach {
                    item(
                        key = it.id
                    ) {
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
                }

                item {
                    Button(
                        onClick = {
                            GlobalScope.launch {
                                val baseSet = excercise.sets.maxByOrNull { it.date.toEpochMilliseconds() }

                                if (baseSet != null) {
                                    dependencies.database.setDataSource.save(
                                        set = baseSet.copy(
                                            id = uuid4().toString(),
                                            // increment date by one to ensure this new list is the "Last Set"
                                            date = baseSet.date.plus(1, DateTimeUnit.MILLISECOND)
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        Text(stringResource(Res.string.excercise_screen_duplicate_cta))
                    }
                }

                item {
                    val coordinator = LocalNavCoordinator.current
                    Button(
                        onClick = {
                            coordinator.present(Destination.EditSet(
                                variationId = excercise.variation.id,
                            ))
                        }
                    ) {
                        Text(stringResource(Res.string.excercise_screen_new_set_cta))
                    }
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
