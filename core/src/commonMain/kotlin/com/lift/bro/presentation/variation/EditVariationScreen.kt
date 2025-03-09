@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.variation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.spacing
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class UOM(val value: String) {
    KG("kg"),
    POUNDS("lbs")
}

@Composable
fun rememberVariation(id: String?): MutableState<Variation> {
    return remember {
        mutableStateOf(
            dependencies.database.variantDataSource.get(id ?: "") ?: Variation()
        )
    }
}

@Composable
fun rememberLift(id: String?): MutableState<Lift?> {
    val lift = mutableStateOf<Lift?>(null)

    LaunchedEffect(id) {
        dependencies.database.liftDataSource.get(id)
            .collectLatest {
                lift.value = it
            }
    }

    return remember { lift }
}

@Composable
fun EditVariationScreen(
    id: String,
    database: LBDatabase = dependencies.database,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    variationSaved: () -> Unit,
) {
    var variation by rememberVariation(id)
    LiftingScaffold(
        title = id.let { "Edit Variation" },
        fabIcon = Icons.Default.Edit,
        contentDescription = "Save Variant",
        fabClicked = {
            coroutineScope.launch {
                database.variantDataSource.save(
                    id = variation.id,
                    name = variation.name,
                    liftId = variation.lift!!.id,
                )
                variationSaved()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {

            Row {
                TextField(
                    value = variation.name ?: "",
                    onValueChange = { variation = variation.copy(name = it) },
                    placeholder = { Text("ex: Front or Back Squat") },
                    trailingIcon = {
                        var showDropdown by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                showDropdown = true
                            },
                            colors = ButtonDefaults.textButtonColors()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    text = variation.lift?.name ?: ""
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = {
                                showDropdown = false
                            }
                        ) {
                            val lists by dependencies.database.liftDataSource.getAll()
                                .collectAsStateWithLifecycle(emptyList())

                            lists.forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(text = it.name)
                                    },
                                    onClick = {
                                        variation = variation.copy(lift = it)
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
