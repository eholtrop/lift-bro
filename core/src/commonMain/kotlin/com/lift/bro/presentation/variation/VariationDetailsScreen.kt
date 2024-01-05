package com.lift.bro.presentation.variation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lift.bro.Settings
import com.lift.bro.data.Set
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.set.EditSetVoyagerScreen
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton
import comliftbrodb.Lift
import comliftbrodb.LiftingSet
import comliftbrodb.Variation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import spacing

internal interface SortingOption {
    object RepDate : SortingOption
    object DateRep : SortingOption
    object Date : SortingOption
    object Reps : SortingOption
}

internal interface FilterOption {

}

@Composable
fun VariationDetailsScreen(
    variationId: String,
    addSetClicked: () -> Unit,
    editClicked: () -> Unit
) {
    val variation = dependencies.database.variantDataSource.get(variationId).executeAsOneOrNull()
    val sets = dependencies.database.setDataSource.getAll(variation?.id ?: "")

    variation?.let {
        VariationDetailsScreen(
            variation = variation,
            sets = sets,
            addSetClicked = addSetClicked,
            editClicked = editClicked,
        )
    }
}

@Composable
internal fun VariationDetailsScreen(
    variation: Variation,
    sets: List<Set>,
    addSetClicked: () -> Unit,
    editClicked: () -> Unit
) {

    LiftingScaffold(
        fabText = "Add Set",
        fabClicked = addSetClicked,
        topBar = {
            TopBar(
                title = variation.name ?: "",
                showBackButton = true,
                trailingContent = {
                    TopBarIconButton(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        onClick = editClicked,
                    )
                }
            )
        }
    ) { padding ->
        val navigator = LocalNavigator.current
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one)
        ) {
            val items = sets.groupBy { it.reps }.entries.toList().sortedBy { it.key }


            items(items) { entry ->

                val sets = entry.value.sortedBy { it.weight }

                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.large
                    ),
                    onClick = {},
                ) {
                    Column {
                        Text(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 52.dp)
                                .padding(
                                    start = MaterialTheme.spacing.half,
                                    top = MaterialTheme.spacing.one
                                ),
                            text = "${entry.key} Rep(s)",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        sets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .clickable(
                                        onClick = { navigator?.push(EditSetVoyagerScreen(setId = set.id)) },
                                        role = Role.Button
                                    )
                                    .padding(MaterialTheme.spacing.half)
                            ) {
                                Text(text = set.formattedWeight)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(text = set.formattedReps)
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
fun RepCard(

) {

}

internal val Set.formattedTempo: String get() = "${this.tempoDown}/${this.tempoHold}/${this.tempoUp}"

internal val Set.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

internal val Set.formattedWeight: String get() = "${this.weight} ${Settings.defaultUOM.value}"

internal val Set.formattedMax: String get() = "${this.reps} x ${this.formattedTempo} @ ${this.formattedWeight}"