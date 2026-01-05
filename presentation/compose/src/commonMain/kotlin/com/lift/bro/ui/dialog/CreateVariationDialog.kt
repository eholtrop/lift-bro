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
import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.DropDownButton
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

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
                            val lifts by dependencies.liftRepository.listenAll()
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
                                dependencies.database.variantDataSource.save(currentVariation)
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
