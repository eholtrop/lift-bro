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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.fullName
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

                    AnimatedVisibility(set.variation != null) {
                        Box(
                            modifier = Modifier.padding(
                                vertical = MaterialTheme.spacing.half,
                                horizontal = MaterialTheme.spacing.one
                            ).animateContentSize()
                        ) {
                            val weight = set.weight ?: 0.0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (liftMax != null) {
                                    val percentage =
                                        ((weight / liftMax.weight) * 100).toInt()
                                    Text(
                                        text = stringResource(
                                            Res.string.weight_selector_chin_title,
                                            percentage,
                                            variation?.lift?.name ?: "",
                                        ),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                if (variationMax != null) {
                                    val percentage =
                                        ((weight / variationMax.weight) * 100).toInt()
                                    Text(
                                        text = stringResource(
                                            Res.string.weight_selector_chin_subtitle,
                                            percentage,
                                            variation?.fullName ?: "",
                                        ),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.one)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large
                        )
                        .clickable(
                            onClick = {
                                showVariationDialog = true
                            },
                            role = Role.DropdownList
                        )
                        .padding(MaterialTheme.spacing.one),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = variation?.fullName
                                ?: stringResource(Res.string.edit_set_screen_variation_selector_empty_state_title),
                            style = MaterialTheme.typography.titleLarge,
                        )

                        Space(MaterialTheme.spacing.one)

                        if (variation == null) {
                            Text(stringResource(Res.string.edit_set_screen_encouragement))
                        } else {
                            val sets = dependencies.database.setDataSource.getAllForLift(
                                variation.lift?.id ?: ""
                            )

                            val liftMax = sets.maxByOrNull { it.weight }
                            val variationMax = sets.filter { it.variationId == variation.id }
                                .maxByOrNull { it.weight }
                            Text(
                                text = stringResource(
                                    Res.string.edit_set_screen_variation_selector_lift_max,
                                    variation.lift?.name ?: stringResource(Res.string.edit_set_screen_variation_selector_no_lift_max),
                                    liftMax?.formattedWeight() ?: stringResource(Res.string.edit_set_screen_variation_selector_no_lift_max),
                                )
                            )
                            Text(
                                text = stringResource(
                                    Res.string.edit_set_screen_variation_selector_variation_max,
                                    variation.fullName,
                                    variationMax?.formattedWeight() ?: stringResource(Res.string.edit_set_screen_variation_selector_no_variation_max),
                                )
                            )
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
