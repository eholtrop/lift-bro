package com.lift.bro.presentation.variation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lift.bro.Settings
import com.lift.bro.data.Set
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.set.EditSetVoyagerScreen
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton
import comliftbrodb.LiftingSet
import spacing


@Composable
fun VariationDetailsScreen(
    variationId: String,
    addSetClicked: () -> Unit,
    editClicked: () -> Unit
) {
    val variation = dependencies.database.variantDataSource.get(variationId).executeAsOneOrNull()

    LiftingScaffold(
        fabText = "Add Set",
        fabClicked = addSetClicked,
        topBar = {
            TopBar(
                title = variation?.name ?: "",
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
        val sets = dependencies.database.setDataSource.getAll(variation?.id ?: "")
        val navigator = LocalNavigator.current
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one)
        ) {
            items(sets) { set ->
                Column(
                    modifier = Modifier.clickable(
                        onClick = { navigator?.push(EditSetVoyagerScreen(setId = set.id)) },
                        role = Role.Button
                    ),
                ) {
                    Text(text = set.formattedWeight)
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.quarter))
                    Text(text = set.formattedReps)
                    Divider(thickness = 1.dp)
                }
            }
        }
    }
}

internal val Set.formattedTempo: String get() = "${this.tempoDown}/${this.tempoHold}/${this.tempoUp}"

internal val Set.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

internal val Set.formattedWeight: String get() = "${this.weight} ${Settings.defaultUOM.value}"

internal val Set.formattedMax: String get() = "${this.reps} x ${this.formattedTempo} @ ${this.formattedWeight}"