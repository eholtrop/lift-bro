@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.lift

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.VariationTextField
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_lift_screen_title
import lift_bro.core.generated.resources.edit_lift_info_dialog_text
import lift_bro.core.generated.resources.edit_lift_info_dialog_title
import lift_bro.core.generated.resources.edit_lift_screen_add_variation_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_delete_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_lift_name_placeholder
import lift_bro.core.generated.resources.edit_lift_screen_lift_warning_dialog_text
import lift_bro.core.generated.resources.edit_lift_screen_title
import lift_bro.core.generated.resources.edit_lift_screen_variation_delete_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_warning_dialog_negative_cta
import lift_bro.core.generated.resources.edit_lift_screen_warning_dialog_positive_cta
import lift_bro.core.generated.resources.edit_lift_screen_warning_dialog_title
import lift_bro.core.generated.resources.edit_lift_variation_heading
import lift_bro.core.generated.resources.edit_lift_variation_warning_dialog_text
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import tv.dpal.flowvi.Interactor

@Composable
fun EditLiftScreen(
    liftId: String? = null,
    liftSaved: () -> Unit,
    liftDeleted: () -> Unit,
) {
    EditLiftScreen(
        interactor = if (liftId != null) rememberEditLiftInteractor(liftId) else rememberCreateLiftInteractor(),
        liftSaved = liftSaved,
        liftDeleted = liftDeleted,
    )
}

@Composable
internal fun EditLiftScreen(
    interactor: Interactor<EditLiftState?, EditLiftEvent>,
    liftSaved: () -> Unit,
    liftDeleted: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    val state by interactor.state.collectAsState()

    var showLiftDeleteWarning by remember { mutableStateOf(false) }

    if (showLiftDeleteWarning) {
        WarningDialog(
            text = stringResource(Res.string.edit_lift_screen_lift_warning_dialog_text),
            onDismiss = { showLiftDeleteWarning = false },
            onConfirm = {
                coroutineScope.launch {
                    interactor(EditLiftEvent.DeleteLift)
                    liftDeleted()
                }
            }
        )
    }

    state?.let { state ->
        LiftingScaffold(
            title = {
                Text(
                    if (state.id != null) {
                        stringResource(Res.string.edit_lift_screen_title)
                    } else {
                        stringResource(
                            Res.string.create_lift_screen_title
                        )
                    }
                )
            },
            trailingContent = {
                if (state.showDelete) {
                    TopBarIconButton(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.edit_lift_screen_delete_cta_content_description),
                        onClick = {
                            showLiftDeleteWarning = true
                        },
                    )
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).animateContentSize().fillMaxWidth(),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                stickyHeader {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        var name by remember(state.name) { mutableStateOf(state.name) }

                        TextField(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.one),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            value = name,
                            onValueChange = {
                                name = it
                                interactor(EditLiftEvent.NameChanged(it))
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.edit_lift_screen_lift_name_placeholder),
                                    textAlign = TextAlign.Center,
                                )
                            },
                            textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                            trailingIcon = {
                                InfoDialogButton(
                                    dialogTitle = { Text(stringResource(Res.string.edit_lift_info_dialog_title)) },
                                    dialogMessage = {
                                        Text(
                                            text = stringResource(Res.string.edit_lift_info_dialog_text),
                                        )
                                    },
                                )
                            }
                        )
                        Space(MaterialTheme.spacing.two)

                        AnimatedVisibility(
                            visible = state.name.isNotBlank() || state.variations.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.semantics {
                                        heading()
                                    },
                                    text = stringResource(Res.string.edit_lift_variation_heading),
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Space(MaterialTheme.spacing.half)
                                IconButton(
                                    onClick = {
                                        interactor(EditLiftEvent.AddVariation)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(
                                            Res.string.edit_lift_screen_add_variation_cta_content_description
                                        ),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                itemsIndexed(
                    state.variations,
                    { index, variation -> variation.id }
                ) { index, variation ->

                    var showVariationWarning by remember { mutableStateOf(false) }

                    if (showVariationWarning) {
                        WarningDialog(
                            text = stringResource(Res.string.edit_lift_variation_warning_dialog_text),
                            onDismiss = { showVariationWarning = false },
                            onConfirm = {
                                interactor(EditLiftEvent.VariationRemoved(variation))
                                showVariationWarning = false
                            }
                        )
                    }
                    VariationTextField(
                        modifier = Modifier.animateItem(),
                        focusRequester = FocusRequester(),
                        variation = variation,
                        onNameChanged = {
                            interactor(EditLiftEvent.VariationNameChanged(variation, it))
                        },
                        action = {
                            IconButton(onClick = {
                                if (variation.eMax != null || variation.oneRepMax != null) {
                                    showVariationWarning = true
                                } else {
                                    interactor(EditLiftEvent.VariationRemoved(variation))
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(
                                        Res.string.edit_lift_screen_variation_delete_cta_content_description
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WarningDialog(
    title: String = stringResource(Res.string.edit_lift_screen_warning_dialog_title),
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(),
                onClick = onConfirm
            ) { Text(stringResource(Res.string.edit_lift_screen_warning_dialog_positive_cta)) }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) { Text(stringResource(Res.string.edit_lift_screen_warning_dialog_negative_cta)) }
        },
        title = { Text(title) },
        icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null) },
        text = { Text(text) },
    )
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

class EditLiftStateProvider: PreviewParameterProvider<EditLiftState> {
    override val values: Sequence<EditLiftState>
        get() = sequenceOf(
            // New lift - empty
            EditLiftState(
                id = null,
                name = "",
                variations = emptyList()
            ),
            // New lift with name only
            EditLiftState(
                id = null,
                name = "Squat",
                variations = emptyList()
            ),
            // Lift with one variation
            EditLiftState(
                id = "lift1",
                name = "Bench Press",
                variations = listOf(
                    Variation(
                        lift = com.lift.bro.domain.models.Lift(
                            name = "Bench Press",
                            color = 0xFF4CAF50uL
                        ),
                        name = "Flat Bench",
                        favourite = true
                    )
                )
            ),
            // Lift with multiple variations
            EditLiftState(
                id = "lift2",
                name = "Deadlift",
                variations = listOf(
                    Variation(
                        lift = com.lift.bro.domain.models.Lift(
                            name = "Deadlift",
                            color = 0xFFFF5722uL
                        ),
                        name = "Conventional",
                        favourite = true
                    ),
                    Variation(
                        lift = com.lift.bro.domain.models.Lift(
                            name = "Deadlift",
                            color = 0xFFFF5722uL
                        ),
                        name = "Sumo",
                        favourite = false
                    ),
                    Variation(
                        lift = com.lift.bro.domain.models.Lift(
                            name = "Deadlift",
                            color = 0xFFFF5722uL
                        ),
                        name = "Romanian",
                        favourite = false
                    )
                )
            )
        )
}

@Preview
@Composable
fun EditLiftScreenPreview(
    @PreviewParameter(EditLiftStateProvider::class) state: EditLiftState,
) {
    PreviewAppTheme(isDarkMode = false) {
        LiftingScaffold(
            title = { Text("Edit Lift") },
            trailingContent = {
                if (state.showDelete) {
                    TopBarIconButton(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        onClick = {}
                    )
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxWidth(),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                item {
                    Text(
                        text = state.name.ifBlank { "Enter lift name" },
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                itemsIndexed(state.variations) { _, variation ->
                    Text(
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.medium
                            )
                            .padding(MaterialTheme.spacing.one),
                        text = "${variation.name ?: "Unnamed"} ${state.name}"
                    )
                }
            }
        }
    }
}
