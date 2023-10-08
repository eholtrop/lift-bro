package com.lift.bro.presentation.variation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton


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
    ) {
        LazyColumn {

        }
    }
}