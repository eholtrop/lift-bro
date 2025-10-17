@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTarget
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.lift.bro.di.dependencies
import com.lift.bro.utils.fullName
import com.lift.bro.presentation.Interactor
import com.lift.bro.ui.DateSelector
import com.lift.bro.ui.Fade
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.formattedWeight
import kotlinx.datetime.Instant
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_delete_acc_label
import lift_bro.core.generated.resources.edit_set_screen_encouragement
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_placeholder
import lift_bro.core.generated.resources.edit_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_empty_state_title
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_lift_max
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_no_lift_max
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_no_variation_max
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_variation_max
import lift_bro.core.generated.resources.weight_selector_chin_subtitle
import lift_bro.core.generated.resources.weight_selector_chin_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditSetScreen(
    variationId: String?,
    date: Instant?,
) {
    EditSetScreen(
        interactor = rememberCreateSetInteractor(
            variationId = variationId,
            date = date
        ),
    )
}

@Composable
fun EditSetScreen(
    setId: String,
) {
    EditSetScreen(
        interactor = rememberEditSetInteractor(
            setId = setId,
        ),
    )
}

@Composable
fun EditSetScreen(
    interactor: Interactor<EditSetState?, EditSetEvent>,
) {
    val state by interactor.state.collectAsState()

    state?.let { set ->
        EditSetScreen(
            set = set,
            dateChanged = {
                interactor(EditSetEvent.DateSelected(it))
            },
            deleteSetClicked = {
                interactor(EditSetEvent.DeleteSetClicked)
            },
            repChanged = {
                interactor(EditSetEvent.RepChanged(it))
            },
            weightChanged = {
                interactor(EditSetEvent.WeightChanged(it))
            },
            rpeChanged = {
                interactor(EditSetEvent.RpeChanged(it))
            },
            eccChanged = {
                interactor(EditSetEvent.EccChanged(it))
            },
            isoChanged = {
                interactor(EditSetEvent.IsoChanged(it))
            },
            conChanged = {
                interactor(EditSetEvent.ConChanged(it))
            },
            notesChanged = {
                interactor(EditSetEvent.NotesChanged(it))
            },
            variationChanged = {
                interactor(EditSetEvent.VariationSelected(it))
            },
        )
    }
}

@Composable
fun EditSetScreen(
    set: EditSetState,
    deleteSetClicked: () -> Unit,
    repChanged: (Long?) -> Unit = {},
    weightChanged: (Double?) -> Unit = {},
    rpeChanged: (Int?) -> Unit = {},
    dateChanged: (Instant) -> Unit = {},
    eccChanged: (Int?) -> Unit = {},
    isoChanged: (Int?) -> Unit = {},
    conChanged: (Int?) -> Unit = {},
    notesChanged: (String) -> Unit = {},
    variationChanged: (variationId: String) -> Unit = {},
) {
    var showVariationDialog by remember { mutableStateOf(false) }

    val variation = set.variation

    LiftingScaffold(
        title = { Text(stringResource(if (set.id != null) Res.string.create_set_screen_title else Res.string.edit_set_screen_title)) },
        trailingContent = {
            Fade(visible = set.saveEnabled) {
                TopBarIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.edit_set_screen_delete_acc_label),
                    onClick = deleteSetClicked
                )
            }
        },
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    RepWeightSelector(
                        set = set,
                        repChanged = repChanged,
                        weightChanged = weightChanged,
                        rpeChanged = rpeChanged,
                    )

                    val sets = dependencies.database.setDataSource.getAllForLift(
                        variation?.lift?.id ?: "",
                        Long.MAX_VALUE,
                    )

                    val liftMax = sets.maxByOrNull { it.weight }
                    val variationMax = sets.filter { it.variationId == variation?.id }
                        .maxByOrNull { it.weight }

                    if (set.variation != null) {
                        Box(
                            modifier = Modifier.padding(
                                vertical = MaterialTheme.spacing.one,
                                horizontal = MaterialTheme.spacing.one
                            ).animateContentSize()
                        ) {
                            val weight = set.weight ?: 0.0
                            Column(
                                modifier = Modifier.clickable(
                                    onClick = {
                                        showVariationDialog = true
                                    }
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    text = buildAnnotatedString {
                                        if (liftMax == null && variationMax == null) {
                                            append(set.variation.fullName)
                                        } else {
                                            liftMax?.let {
                                                append(buildSetLiftSubtitle(
                                                    value = ((weight / liftMax.weight) * 100).toFloat(),
                                                    name = set.variation.fullName
                                                ))
                                            }
                                            variationMax?.let {
                                                append("\n")
                                                append(
                                                    buildSetLiftSubtitle(
                                                        value = ((weight / variationMax.weight) * 100).toFloat(),
                                                        name = set.variation.fullName
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Button(
                            colors = ButtonDefaults.outlinedButtonColors(),
                            onClick = {
                                showVariationDialog = true
                            }
                        ) {
                            Text("Select Variation")
                        }
                    }
                }
            }

            item {
                TempoSelector(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
                    up = set.concentric?.toInt(),
                    hold = set.isometric?.toInt(),
                    down = set.eccentric?.toInt(),
                    downChanged = eccChanged,
                    holdChanged = isoChanged,
                    upChanged = conChanged,
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

                DateSelector(
                    date = set.date,
                    dateChanged = dateChanged
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.one),
                ) {
                    Text(
                        stringResource(Res.string.edit_set_screen_extra_notes_label),
                        style = MaterialTheme.typography.titleMedium
                    )
                    var notes by remember(set.notes) { mutableStateOf(set.notes) }

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = notes,
                        singleLine = true,
                        placeholder = {
                            Text(stringResource(Res.string.edit_set_screen_extra_notes_placeholder))
                        },
                        onValueChange = {
                            notes = it
                            notesChanged(it)
                        },
                    )
                }
            }
        }
    }

    VariationSearchDialog(
        visible = showVariationDialog,
        textFieldPlaceholder = stringResource(Res.string.edit_set_screen_variation_selector_empty_state_title),
        onDismissRequest = { showVariationDialog = false },
        onVariationSelected = {
            showVariationDialog = false
            variationChanged(it.id)
        }
    )
}

@Composable
private fun buildSetLiftSubtitle(
    value: Float,
    name: String,
): AnnotatedString {
    return buildAnnotatedString {
        val str = stringResource(
            Res.string.weight_selector_chin_title,
            value,
            name,
        )

        append(
            str.substring(0, str.indexOf(name))
        )

        withStyle(
            MaterialTheme.typography.titleMedium
                .copy(
                    color = MaterialTheme.colorScheme.primary,
                ).toSpanStyle(),
        ) {

            append(
                name,
            )
        }

        append(
            str.substring(
                str.indexOf(name) + name.length,
            )
        )
    }
}
