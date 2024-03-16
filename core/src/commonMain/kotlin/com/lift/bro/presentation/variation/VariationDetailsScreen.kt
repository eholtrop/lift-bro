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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lift.bro.Settings
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
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

    LiftingScaffold(
        fabText = "Add Set",
        fabClicked = addSetClicked,
        topBar = {
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
        }
    ) { padding ->
        val navigator = LocalNavigator.current
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.one)
        ) {
            val items = sets.groupBy { it.date.toLocalDate() }.entries.toList()
                .sortedByDescending { it.key }


            items(items) { entry ->

                Card(
                    onClick = {},
                ) {
                    Column(
                        modifier = Modifier.padding(all = MaterialTheme.spacing.one)
                    ) {
                        Text(
                            text = entry.key.toString(pattern = "EEEE MMM, d"),
                            style = MaterialTheme.typography.titleLarge
                        )
                        entry.value.forEach { set ->
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
                                    text = "${set.reps} x ${set.formattedWeight}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Row {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Down"
                                    )
                                    Text(
                                        text = set.tempoDown.toString(),
                                    )
                                    Space(MaterialTheme.spacing.half)
                                    Text(
                                        text = "--",
                                    )
                                    Text(
                                        text = set.tempoHold.toString(),
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Up"
                                    )
                                    Text(
                                        text = set.tempoUp.toString(),
                                    )
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
fun RepCard(

) {

}

internal val LBSet.formattedTempo: String get() = "${this.tempoDown}/${this.tempoHold}/${this.tempoUp}"

internal val LBSet.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

internal val LBSet.formattedWeight: String get() = "${this.weight} ${Settings.defaultUOM.value}"

internal val LBSet.formattedMax: String get() = "${this.reps} x ${this.formattedTempo} @ ${this.formattedWeight}"