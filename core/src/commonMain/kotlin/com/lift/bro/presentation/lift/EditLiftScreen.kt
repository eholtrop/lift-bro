@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.lift

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class EditLiftState(
    val lift: Lift,
    val variations: SnapshotStateList<EditLiftVariationState>,
    val isNew: Boolean,
)

data class EditLiftVariationState(
    val id: String = uuid4().toString(),
    val lift: Lift,
    val name: String? = null,
    val isNew: Boolean,
)

@Composable
fun EditLiftScreen(
    liftId: String? = null,
    liftSaved: () -> Unit,
    liftDeleted: () -> Unit,
    editVariationClicked: (Variation) -> Unit,
) {
    val lift by dependencies.database.liftDataSource.get(liftId).collectAsState(null)
    val variations by dependencies.database.variantDataSource.listenAll(liftId ?: "")
        .collectAsState(emptyList())

    EditLiftScreen(
        lift = lift,
        initialVariations = variations,
        liftSaved = liftSaved,
        liftDeleted = liftDeleted,
    )
}

@Composable
private fun WarningDialog(
    title: String = "Warning",
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onConfirm) { Text("Okay!") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Nevermind") } },
        title = { Text(title) },
        icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning") },
        text = { Text(text) },
    )
}

@Composable
internal fun EditLiftScreen(
    lift: Lift?,
    initialVariations: List<Variation>,
    liftSaved: () -> Unit,
    liftDeleted: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var thisLift by remember(lift) { mutableStateOf(lift ?: Lift()) }
    val variations =
        remember(initialVariations) { (initialVariations + Variation()).toMutableStateList() }
    var showLiftDeleteWarning by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    if (showLiftDeleteWarning) {
        WarningDialog(
            text = "This will delete all variations and sets for this lift, This cannot be undone",
            onDismiss = { showLiftDeleteWarning = false },
            onConfirm = {
                GlobalScope.launch {
                    variations.forEach {
                        dependencies.database.setDataSource.deleteAll(it.id)
                        dependencies.database.variantDataSource.delete(it.id)
                    }
                    dependencies.database.liftDataSource.delete(lift?.id ?: "")
                    liftDeleted()
                }
            }
        )
    }

    LiftingScaffold(
        title = if (lift != null) "Edit Lift" else "Create Lift",
        trailingContent = {
            TopBarIconButton(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                onClick = {
                    showLiftDeleteWarning = true
                },
            )
        },
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Edit,
            contentDescription = "Save Lift",
            fabClicked = {
                dependencies.database.liftDataSource.save(
                    thisLift
                )
                coroutineScope.launch {
                    variations.forEach {
                        dependencies.database.variantDataSource.save(
                            id = it.id,
                            liftId = thisLift.id,
                            name = it.name
                        )
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    value = thisLift.name,
                    onValueChange = { thisLift = thisLift.copy(name = it) },
                    placeholder = {
                        Text(
                            text = "ex: Squat, Bench Press, Deadlift",
                            textAlign = TextAlign.Center,
                        )
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
                )
                InfoDialogButton(
                    dialogTitle = { Text("What is a Lift?") },
                    dialogMessage = {
                        Text(
                            text =
                                "A group of Variations/Movements\n" +
                                        "\n" +
                                        "ex: A Squat can have many variations such as Front and Back Squat\n" +
                                        "\n" +
                                        "Once you name your lift you can start creating Variations of that lift!\n" +
                                        "Think of it as the \"suffix\" of a movement\n" +
                                        "\n" +
                                        "Romanian *Deadlift*\n" +
                                        "Front *Squat*\n" +
                                        "Incline *Bench Press*\n"
                        )
                    },
                )
            }
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
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                itemsIndexed(variations) { index, variation ->

                    var showVariationWarning by remember { mutableStateOf(false) }

                    if (showVariationWarning) {
                        WarningDialog(
                            text = "This will delete all sets for this variation, This cannot be undone",
                            onDismiss = { showVariationWarning = false },
                            onConfirm = {
                                GlobalScope.launch {
                                    dependencies.database.setDataSource.deleteAll(variation.id)
                                    dependencies.database.variantDataSource.delete(variation.id)
                                    variations.remove(variation)
                                }
                            }
                        )
                    }

                    VariationItem(
                        variation = variation,
                        liftName = thisLift.name,
                        onNameChange = { variations[index] = variations[index].copy(name = it) },
                        onDelete = {
                            if (initialVariations.contains(variation)) {
                                showVariationWarning = true
                            } else {
                                variations.remove(variation)
                            }
                        }
                    )
                }
            }

            var focusLastItem by remember { mutableStateOf(false) }
            LaunchedEffect(focusLastItem) {
                if (focusLastItem) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                    focusLastItem = false
                }
            }

            Button(
                onClick = {
                    variations.add(Variation())
                    focusLastItem = true
                },
            ) {
                Text("Add Variation")
            }
        }
    }
}

@Composable
private fun VariationItem(
    variation: Variation,
    liftName: String,
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = modifier.weight(1f),
            value = variation.name ?: "",
            singleLine = true,
            onValueChange = onNameChange,
            placeholder = { Text("e.g., Back, Front, Incline") },
            suffix = { if(liftName.isNotBlank()) Text(liftName) },
            colors = TextFieldDefaults.transparentColors()
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Variation")
        }
    }
}

@Composable
fun TextFieldDefaults.transparentColors(): TextFieldColors = TextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
)