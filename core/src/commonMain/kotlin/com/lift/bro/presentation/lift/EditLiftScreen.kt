@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.lift

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarIconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

data class EditLiftState(
    val variations: List<Variation>
)

data class EditLiftVariationState(
    val id: String,
    val lift: Lift?,
    val name: String?,
    val isNew: Boolean,
)

@Composable
fun EditLiftScreen(
    liftId: String? = null,
    liftSaved: () -> Unit,
    liftDeleted: () -> Unit,
    editVariationClicked: (Variation) -> Unit,
    database: LBDatabase = dependencies.database,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val variations = mutableStateListOf(
        *database.variantDataSource.getAll(liftId ?: "").map {
            EditLiftVariationState(
                id = it.id,
                lift = it.lift,
                name = it.name,
                isNew = false,
            )
        }.toTypedArray()
    )

    var lift by remember {
        mutableStateOf(
            Lift(
                id = uuid4().toString(),
                name = "",
                color = null
            )
        )
    }

    LaunchedEffect("get lift") {
        database.liftDataSource.get(liftId ?: "")
            .filterNotNull()
            .collectLatest {
                lift = it
            }
    }

    LaunchedEffect("Fix variations") {
        if (variations.isEmpty()) {
            variations.add(
                EditLiftVariationState(
                    id = uuid4().toString(),
                    lift = lift,
                    name = "",
                    isNew = true
                )
            )
        }
    }

    var showDeleteWarning by remember { mutableStateOf(false) }
    if (showDeleteWarning) {
        AlertDialog(
            onDismissRequest = {
                showDeleteWarning = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        GlobalScope.launch {
                            variations.forEach {
                                database.setDataSource.deleteAll(it.id)
                                database.variantDataSource.delete(it.id)
                            }
                            database.liftDataSource.delete(lift.id)
                            liftDeleted()
                        }
                    }
                ) {
                    Text("Okay!")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDeleteWarning = false
                    }
                ) {
                    Text("Nevermind")
                }
            },
            title = {
                Text("Warning")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning"
                )
            },
            text = {
                Text("This will delete all variations and sets for this lift, This cannot be undone")
            },
        )
    }

    LiftingScaffold(
        title = liftId?.let { "Edit Lift" } ?: "Create Lift",
        trailingContent = {
            TopBarIconButton(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                onClick = {
                    showDeleteWarning = true
                },
            )
        },
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Edit,
            contentDescription = "Save Lift",
            fabClicked = {
                database.liftDataSource.save(
                    lift
                )
                coroutineScope.launch {
                    variations.forEach {
                        if (it.name?.isNotBlank() == true) {
                            database.variantDataSource.save(
                                id = it.id,
                                liftId = it.lift?.id!!,
                                name = it.name
                            )
                        } else {
                            database.variantDataSource.delete(it.id)
                            database.setDataSource.deleteAll(it.id)
                        }
                    }
                }
                liftSaved()
            },
        ),
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Space(MaterialTheme.spacing.one)
            TextField(
                value = lift.name,
                onValueChange = { lift = lift.copy(name = it) },
                placeholder = { Text("Squat, Bench Press, Deadlift") },
                textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
            )
            Space(MaterialTheme.spacing.two)

            Text(
                modifier = Modifier.align(Alignment.Start)
                    .padding(start = MaterialTheme.spacing.one)
                    .semantics {
                        heading()
                    },
                text = "Variations",
                style = MaterialTheme.typography.titleLarge
            )

            LazyColumn(
                contentPadding = PaddingValues(MaterialTheme.spacing.one)
            ) {

                itemsIndexed(variations) { index, variation ->

                    val sets = database.setDataSource.getAll(variation.id)

                    Row(
                        modifier = Modifier
                            .padding(bottom = MaterialTheme.spacing.half),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = variation.name ?: "",
                            singleLine = true,
                            onValueChange = {
                                variations[index] = variations[index].copy(name = it)
                            },
                            enabled = variation.isNew,
                            placeholder = {
                                Text(text = "ex: Back vs Front Squat")
                            },
                            supportingText = if (variation.name.isNullOrBlank() && sets.isNotEmpty()) {
                                {
                                    Text(text = "All sets will be deleted if saved")
                                }
                            } else null,
                            isError = variation.name.isNullOrBlank() && sets.isNotEmpty(),
                            suffix = {
                                Text(text = lift.name)
                            }
                        )

                        Space(MaterialTheme.spacing.one)

                        if (variation.isNew) {
                            IconButton(
                                onClick = { variations.remove(variation) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    editVariationClicked(
                                        Variation(
                                            id = variation.id,
                                            lift = variation.lift,
                                            name = variation.name,
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        }
                    }
                }

                item {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                variations.add(
                                    EditLiftVariationState(
                                        id = uuid4().toString(),
                                        lift = lift,
                                        name = "",
                                        isNew = true
                                    )
                                )
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Variation"
                            )
                        }
                    }
                }
            }
        }
    }
}