package com.lift.bro.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.fullName
import com.lift.bro.presentation.components.DropDownButton
import com.lift.bro.presentation.spacing
import com.lift.bro.ui.DecimalFormat
import com.lift.bro.ui.Space
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateMaxSetDialog(
    modifier: Modifier = Modifier,
    parentLiftId: String,
    onDismissRequest: () -> Unit,
    onSetCreated: () -> Unit,
    properties: DialogProperties = DialogProperties()
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
                val variations = dependencies.database.variantDataSource.getAll(parentLiftId)

                var selectedVariation by remember { mutableStateOf(variations.firstOrNull()) }

                var weight by remember { mutableStateOf<Double?>(null) }
                var reps by remember { mutableStateOf<Int?>(1) }

                val coroutineScope = rememberCoroutineScope()

                var isLoading by remember { mutableStateOf(false) }

                Text(
                    text = "Set your Max!",
                    style = MaterialTheme.typography.titleLarge,
                )

                Space(MaterialTheme.spacing.two)

                Row {
                    TextField(
                        modifier = Modifier.width(52.dp),
                        value = reps?.toString() ?: "",
                        onValueChange = { reps = it.toIntOrNull()},
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )

                    Space(MaterialTheme.spacing.half)
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "x",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Space(MaterialTheme.spacing.half)

                    TextField(
                        modifier = Modifier.weight(1f),
                        value = DecimalFormat.formatWeight(weight),
                        onValueChange = { weight = it.toDoubleOrNull() },
                        supportingText = {
                            Text("weight in lbs")
                        },
                        trailingIcon = {
                            DropDownButton(
                                buttonText = selectedVariation?.fullName ?: "",
                            ) {
                                variations.forEach { variation ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(variation.fullName)
                                        },
                                        onClick = {
                                            selectedVariation = variation
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
                        enabled = weight != null && reps != null,
                        onClick = {
                            isLoading = true
                            coroutineScope.launch(context = Dispatchers.IO) {
                                val newId = uuid4().toString()
                                dependencies.database.setDataSource.save(
                                    set = LBSet(
                                        id = uuid4().toString(),
                                        variationId = selectedVariation!!.id,
                                        weight = weight!!,
                                        reps = reps!!.toLong(),
                                        notes = ""
                                    )
                                )
                                onSetCreated()
                            }
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Create")
                    }
                }

            }
        }
    )
}