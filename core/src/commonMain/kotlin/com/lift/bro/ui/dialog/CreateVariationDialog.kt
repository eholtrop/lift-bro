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
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.Space
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateVariationDialog(
    modifier: Modifier = Modifier,
    parentLiftId: String,
    onDismissRequest: () -> Unit,
    onVariationCreated: (String) -> Unit,
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
                val parentLift by dependencies.database.liftDataSource.get(parentLiftId)
                    .collectAsState(null)

                var variationName by remember { mutableStateOf("") }

                val coroutineScope = rememberCoroutineScope()

                var isLoading by remember { mutableStateOf(false) }

                Text(
                    text = "Create Variation",
                    style = MaterialTheme.typography.titleLarge,
                )

                Space(MaterialTheme.spacing.two)

                parentLift?.let { lift ->
                    TextField(
                        value = variationName,
                        onValueChange = { variationName = it },
                        suffix = {
                            Text(lift.name)
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
                        enabled = variationName.isNotBlank(),
                        onClick = {
                            isLoading = true
                            coroutineScope.launch(context = Dispatchers.IO) {
                                val newId = uuid4().toString()
                                dependencies.database.variantDataSource.save(
                                    id = newId,
                                    liftId = parentLift!!.id,
                                    name = variationName
                                )
                                onVariationCreated(newId)
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