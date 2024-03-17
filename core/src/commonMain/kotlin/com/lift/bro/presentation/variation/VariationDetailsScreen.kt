package com.lift.bro.presentation.variation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.Group
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lift.bro.Settings
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.set.EditSetVoyagerScreen
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

private enum class Grouping {
    Date,
    Reps,
    Tempo,
    Weight
}

private fun Grouping.title(set: LBSet) = when (this) {
    Grouping.Date -> "${set.formattedWeight} x ${set.reps}"
    Grouping.Reps -> set.formattedWeight
    Grouping.Tempo -> "${set.formattedWeight} x ${set.reps}"
    Grouping.Weight -> set.formattedMax
}

@Composable
fun VariationDetailsScreen(
    variationId: String,
    addSetClicked: () -> Unit,
    editClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    val variation = dependencies.database.variantDataSource.get(variationId)
    val lift by dependencies.database.liftDataSource.get(variation?.liftId).collectAsState(null)
    val sets = dependencies.database.setDataSource.getAll(variation?.id ?: "")

    variation?.let {
        VariationDetailsScreen(
            variation = variation,
            lift = lift,
            sets = sets,
            addSetClicked = addSetClicked,
            editClicked = editClicked,
            setClicked = setClicked,
        )
    }
}

@Composable
internal fun VariationDetailsScreen(
    variation: Variation,
    lift: Lift?,
    sets: List<LBSet>,
    addSetClicked: () -> Unit,
    editClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {

    var grouping by rememberSaveable { mutableStateOf(Grouping.Date) }

    LiftingScaffold(
        fabText = "Add Set",
        fabClicked = addSetClicked,
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopBar(
                    title = "${variation.name} ${lift?.name}",
                    showBackButton = true,
                    trailingContent = {
                        TopBarIconButton(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            onClick = editClicked,
                        )
                    }
                )

                Row {
                    var expanded by rememberSaveable { mutableStateOf(false) }

                    Button(
                        onClick = { expanded = true }
                    ) {
                        Text(text = grouping.toString())
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Grouping.values().forEach {
                            DropdownMenuItem(
                                text = { Text(it.toString()) },
                                onClick = {
                                    grouping = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->


        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.one)
        ) {

            val items: List<Pair<String, List<LBSet>>> = when (grouping) {
                Grouping.Date -> sets.groupBy { it.date.toLocalDate() }.toList()
                    .sortedByDescending { it.first }
                    .map { Pair(it.first.toString("EEEE, MMM d"), it.second) }

                Grouping.Reps -> sets.groupBy { it.reps }.toList().sortedByDescending { it.first }
                    .map { Pair("${it.first} Rep(s)", it.second) }

                Grouping.Tempo -> sets.groupBy { it.tempo }.toList()
                    .sortedByDescending { it.second.maxOf { it.date.toLocalDate() } }
                    .map { Pair("${it.first.down}/${it.first.hold}/${it.first.up}", it.second) }

                Grouping.Weight -> sets.groupBy { it.weight }.toList()
                    .sortedByDescending { it.first }.map { Pair("${it.first}", it.second) }
            }


            items(items) { entry ->
                Card(
                    onClick = {},
                ) {
                    Column(
                        modifier = Modifier.padding(all = MaterialTheme.spacing.one)
                    ) {
                        Text(
                            text = entry.first,
                            style = MaterialTheme.typography.titleLarge
                        )
                        entry.second.sortedByDescending { it.date }.forEach { set ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .defaultMinSize(minHeight = 52.dp)
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { setClicked(set) }
                                    ),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = grouping.title(set),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                if (grouping != Grouping.Date) {
                                    Text(
                                        text = set.date.toLocalDate().toString("MMM d"),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                if (grouping != Grouping.Tempo) {
                                    set.tempo.render()
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
            }
        }
    }
}

@Composable
fun Tempo.render() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(12.dp),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Down"
        )
        Text(
            text = down.toString(),
            style = MaterialTheme.typography.labelSmall,
        )
        Space(MaterialTheme.spacing.half)
        Text(
            text = "-",
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = hold.toString(),
            style = MaterialTheme.typography.labelSmall,
        )
        Icon(
            modifier = Modifier.size(12.dp),
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Up"
        )
        Text(
            text = up.toString(),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun RepCard(

) {

}

internal val LBSet.formattedTempo: String get() = "${this.tempo.down}/${this.tempo.hold}/${this.tempo.up}"

internal val LBSet.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

internal val LBSet.formattedWeight: String get() = "${this.weight} ${Settings.defaultUOM.value}"

internal val LBSet.formattedMax: String get() = "${this.reps} x ${this.formattedTempo}"