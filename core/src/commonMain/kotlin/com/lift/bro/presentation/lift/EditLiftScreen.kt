@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.lift

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_lift_screen_title
import lift_bro.core.generated.resources.edit_lift_info_dialog_text
import lift_bro.core.generated.resources.edit_lift_info_dialog_title
import lift_bro.core.generated.resources.edit_lift_screen_add_variation_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_delete_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_lift_name_placeholder
import lift_bro.core.generated.resources.edit_lift_screen_lift_warning_dialog_text
import lift_bro.core.generated.resources.edit_lift_screen_save_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_title
import lift_bro.core.generated.resources.edit_lift_screen_variation_delete_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_variation_name_placeholder
import lift_bro.core.generated.resources.edit_lift_screen_warning_dialog_negative_cta
import lift_bro.core.generated.resources.edit_lift_screen_warning_dialog_positive_cta
import lift_bro.core.generated.resources.edit_lift_screen_warning_dialog_title
import lift_bro.core.generated.resources.edit_lift_variation_heading
import lift_bro.core.generated.resources.edit_lift_variation_warning_dialog_text
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditLiftScreen(
    liftId: String? = null,
    liftSaved: () -> Unit,
    liftDeleted: () -> Unit,
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
internal fun EditLiftScreen(
    lift: Lift?,
    initialVariations: List<Variation>,
    liftSaved: () -> Unit,
    liftDeleted: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var thisLift by remember(lift) { mutableStateOf(lift ?: Lift()) }
    val variations =
        remember(initialVariations) { (initialVariations).toMutableStateList() }
    var showLiftDeleteWarning by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(initialVariations) {
        if (initialVariations.isEmpty()) {
            variations.add(
                Variation(
                    name = ""
                )
            )
        }
    }

    if (showLiftDeleteWarning) {
        WarningDialog(
            text = stringResource(Res.string.edit_lift_screen_lift_warning_dialog_text),
            onDismiss = { showLiftDeleteWarning = false },
            onConfirm = {
                coroutineScope.launch {
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
        title = {
            Text(if (lift != null) stringResource(Res.string.edit_lift_screen_title) else stringResource(Res.string.create_lift_screen_title))
        },
        trailingContent = {
            TopBarIconButton(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.edit_lift_screen_delete_cta_content_description),
                onClick = {
                    showLiftDeleteWarning = true
                },
            )
        },
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Edit,
            contentDescription = stringResource(Res.string.edit_lift_screen_save_cta_content_description),
            fabClicked = {
                dependencies.database.liftDataSource.save(
                    thisLift
                )
                coroutineScope.launch {
                    variations.filter { it.name != null }.forEach {
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
                            text = stringResource(Res.string.edit_lift_screen_lift_name_placeholder),
                            textAlign = TextAlign.Center,
                        )
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
                )
                InfoDialogButton(
                    dialogTitle = { Text(stringResource(Res.string.edit_lift_info_dialog_title)) },
                    dialogMessage = {
                        Text(
                            text = stringResource(Res.string.edit_lift_info_dialog_text),
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
                text = stringResource(Res.string.edit_lift_variation_heading),
                style = MaterialTheme.typography.titleLarge
            )

            LazyColumn(
                modifier = Modifier.animateContentSize().fillMaxWidth(),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                itemsIndexed(variations, { index, variation -> variation.id }) { index, variation ->

                    var showVariationWarning by remember { mutableStateOf(false) }// 1. Create a transition state for each item's visibility
                    val visibilityState = remember {
                        MutableTransitionState(true)
                    }

                    LaunchedEffect(visibilityState.currentState) {
                        if (!visibilityState.currentState && !visibilityState.targetState && variations.contains(
                                variation
                            )
                        ) {
                            dependencies.database.variantDataSource.delete(variation.id)
                            dependencies.database.setDataSource.deleteAll(variation.id)
                            variations.remove(variation)
                        }
                    }

                    if (showVariationWarning) {
                        WarningDialog(
                            text = stringResource(Res.string.edit_lift_variation_warning_dialog_text),
                            onDismiss = { showVariationWarning = false },
                            onConfirm = {
                                visibilityState.targetState = false
                                showVariationWarning = false
                            }
                        )
                    }

                    AnimatedVisibility(
                        visibleState = visibilityState,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        VariationItem(
                            modifier = Modifier.animateItem(),
                            focusRequester = focusRequester,
                            variation = variation,
                            liftName = thisLift.name,
                            onNameChange = {
                                variations[index] = variations[index].copy(name = it)
                            },
                            onDelete = {
                                if (initialVariations.contains(variation)) {
                                    showVariationWarning = true
                                } else {
                                    visibilityState.targetState = false
                                }
                            }
                        )
                    }
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
                    variations.add(Variation(name = ""))
                    focusLastItem = true
                },
            ) {
                Text(stringResource(Res.string.edit_lift_screen_add_variation_cta_content_description))
            }
        }
    }
}

@Composable
private fun WarningDialog(
    title: String = stringResource(Res.string.edit_lift_screen_warning_dialog_title),
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
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
private fun VariationItem(
    variation: Variation,
    liftName: String,
    focusRequester: FocusRequester = FocusRequester(),
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            value = variation.name ?: "",
            singleLine = true,
            onValueChange = onNameChange,
            maxLines = 1,
            placeholder = { Text(stringResource(Res.string.edit_lift_screen_variation_name_placeholder)) },
            suffix = {
                if (liftName.isNotBlank()) {
                    Text(
                        text = if (liftName.length > 12) liftName.substring(
                            0,
                            11
                        ) + "..." else liftName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }
            },
            colors = TextFieldDefaults.transparentColors()
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(Res.string.edit_lift_screen_variation_delete_cta_content_description)
            )
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