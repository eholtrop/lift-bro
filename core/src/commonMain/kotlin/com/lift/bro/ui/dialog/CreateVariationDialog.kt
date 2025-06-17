package com.lift.bro.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.DropDownButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.Space
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

@Composable
fun EditVariationDialog(
    modifier: Modifier = Modifier,
    variationId: String,
    onDismissRequest: () -> Unit,
    onVariationSaved: (String) -> Unit,
    properties: DialogProperties = DialogProperties()
) {
    val variation = dependencies.database.variantDataSource.get(variationId)

    VariationDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Edit Variation",
        properties = properties,
        variation = variation!!,
        onVariationSaved = onVariationSaved,
    )
}


@Composable
fun CreateVariationDialog(
    modifier: Modifier = Modifier,
    parentLiftId: String,
    onDismissRequest: () -> Unit,
    onVariationCreated: (String) -> Unit,
    properties: DialogProperties = DialogProperties()
) {
    val parentLift by dependencies.database.liftDataSource.get(parentLiftId)
        .collectAsState(null)

    if (parentLift != null) {

        VariationDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            title = "Create Variation",
            properties = properties,
            variation = Variation(
                lift = parentLift,
            ),
            onVariationSaved = onVariationCreated,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun VariationDialog(
    modifier: Modifier = Modifier,
    variation: Variation,
    title: String,
    onDismissRequest: () -> Unit,
    onVariationSaved: (String) -> Unit,
    properties: DialogProperties,
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large,
                ).padding(MaterialTheme.spacing.one),
            ) {

                var currentVariation by remember { mutableStateOf(variation) }

                val coroutineScope = rememberCoroutineScope()

                var isLoading by remember { mutableStateOf(false) }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )

                Space(MaterialTheme.spacing.two)

                variation.lift?.let { lift ->
                    TextField(
                        value = currentVariation.name ?: "",
                        onValueChange = { currentVariation = currentVariation.copy(name = it) },
                        trailingIcon = {

                            val lifts by dependencies.database.liftDataSource.listenAll()
                                .collectAsState(emptyList())

                            DropDownButton(
                                buttonText = currentVariation.lift?.name ?: ""
                            ) {
                                lifts.forEach {
                                    DropdownMenuItem(
                                        text = { Text(text = it.name) },
                                        onClick = {
                                            currentVariation = currentVariation.copy(lift = it)
                                        }
                                    )
                                }
                            }
                        }
                    )
                }

                Space(MaterialTheme.spacing.one)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Cancel")
                    }
                    Space(MaterialTheme.spacing.half)
                    Button(
                        enabled = currentVariation.name?.isNotBlank() ?: false,
                        onClick = {
                            isLoading = true
                            coroutineScope.launch(context = Dispatchers.IO) {
                                dependencies.database.variantDataSource.save(
                                    id = currentVariation.id,
                                    liftId = currentVariation.lift!!.id,
                                    name = currentVariation.name
                                )
                                onVariationSaved(currentVariation.id)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Save")
                    }
                }

            }
        }
    )
}