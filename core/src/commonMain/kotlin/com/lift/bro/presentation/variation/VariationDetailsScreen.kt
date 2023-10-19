package com.lift.bro.presentation.variation

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
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
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
        val sets = dependencies.database.setDataSource.getAllByVariation(variation?.id ?: "")
            .executeAsList()
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one)
        ) {
            items(sets) { set ->
                Text(text = set.weight?.toString() ?: ("" + " ${set.unit ?: ""}"))
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.quarter))
                Text(text = set.formattedTempo)
                Divider(thickness = 1.dp)
            }
        }
    }
}

private val LiftingSet.formattedTempo: String get() = "${this.tempoDown}/${this.tempoHold}/${this.tempoUp} x ${this.reps}"