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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.spacing
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Composable
fun EditLiftScreen(
    liftId: String? = null,
    liftSaved: () -> Unit,
    database: LBDatabase = dependencies.database,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val variations = mutableStateListOf(
        *database.variantDataSource.getAll(liftId ?: "").toTypedArray()
    )

    var lift by remember { mutableStateOf<Lift>(Lift(id = uuid4().toString(), name = "")) }

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
                Variation(
                    id = uuid4().toString(),
                    lift = lift,
                    name = "",
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = liftId?.let { "Edit Lift" } ?: "Create Lift",
                showBackButton = true
            )
        },
        floatingActionButton = {
            Button(
                enabled = lift.name.isNotBlank() && variations.any { it.name?.isNotBlank() == true },
                onClick = {
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
                }
            ) {
                Text("Save")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Space(MaterialTheme.spacing.one)
            TextField(
                value = lift.name,
                onValueChange = { lift = lift.copy(name = it) },
                placeholder = { Text("Press, Squat, Deadlift") }
            )
            Space(MaterialTheme.spacing.two)

            Text(
                text = "Variations",
                style = MaterialTheme.typography.headlineSmall
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
                            placeholder = {
                                if (sets.isNotEmpty()) {
                                    Text(text = "Variation and sets will be deleted if saved")
                                } else {
                                    Text(text = "ex: Back vs Front Squat")
                                }
                            }
                        )

                        Space(MaterialTheme.spacing.one)

                        IconButton(
                            onClick = { variations[index] = variations[index].copy(name = "") },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }

                item {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = {
                                variations.add(
                                    Variation(
                                        id = uuid4().toString(),
                                        lift = lift,
                                        name = "",
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