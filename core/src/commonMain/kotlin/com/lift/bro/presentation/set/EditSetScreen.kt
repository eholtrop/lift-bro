@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.fullName
import com.lift.bro.ui.DateSelector
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.VariationCard
import com.lift.bro.ui.dialog.CreateVariationDialog
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.formattedWeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_create_lift_cta
import lift_bro.core.generated.resources.edit_set_screen_delete_acc_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_placeholder
import lift_bro.core.generated.resources.edit_set_screen_save_cta_content_description
import lift_bro.core.generated.resources.edit_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_empty_state_title
import lift_bro.core.generated.resources.weight_selector_chin_subtitle
import lift_bro.core.generated.resources.weight_selector_chin_title
import org.jetbrains.compose.resources.stringResource

data class EditSetState(
    val id: String,
    val variationId: String? = null,
    val weight: Double? = 0.0,
    val reps: Long? = 1,
    val down: Long? = 3,
    val hold: Long? = 1,
    val up: Long? = 1,
    val date: Instant = Clock.System.now(),
    val notes: String = "",
    val rpe: Int? = null,
)

private fun LBSet.toUiState() = EditSetState(
    id = this.id,
    variationId = this.variationId,
    weight = this.weight,
    reps = this.reps,
    down = this.tempo.down,
    hold = this.tempo.hold,
    up = this.tempo.up,
    date = this.date,
    notes = this.notes,
    rpe = this.rpe,
)

private fun EditSetState.toDomain() = LBSet(
    id = this.id,
    variationId = this.variationId!!,
    weight = this.weight!!,
    reps = this.reps!!,
    tempo = Tempo
        (
        down = this.down!!,
        hold = this.hold!!,
        up = this.up!!
    ),
    date = this.date,
    notes = this.notes,
    rpe = this.rpe,
)

@Composable
fun EditSetScreen(
    setId: String?,
    variationId: String?,
    liftId: String?,
    date: Instant?,
    setSaved: () -> Unit,
    createLiftClicked: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var set by remember(setId, variationId, liftId) {
        mutableStateOf(
            dependencies.database.setDataSource.get(setId)?.toUiState() ?: EditSetState(
                id = uuid4().toString(),
                variationId = variationId,
                date = date ?: Clock.System.now()
            )
        )
    }
    var showVariationDialog by remember { mutableStateOf(false) }

    val variation =
        dependencies.database.variantDataSource.get(set.variationId)

    val saveEnabled =
        set.variationId != null && set.reps != null && set.down != null && set.hold != null && set.up != null && set.weight != null

    LiftingScaffold(
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Edit,
            contentDescription = stringResource(Res.string.edit_set_screen_save_cta_content_description),
            fabEnabled = saveEnabled,
            fabClicked = {
                coroutineScope.launch {
                    dependencies.database.setDataSource.save(set.toDomain())
                }
                setSaved()
            },
        ),
        title = { Text(stringResource(if (setId != null) Res.string.create_set_screen_title else Res.string.edit_set_screen_title)) },
        trailingContent = {
            if (setId != null) {
                val navCoordinator = LocalNavCoordinator.current
                TopBarIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.edit_set_screen_delete_acc_label),
                    onClick = {
                        coroutineScope.launch {
                            dependencies.database.setDataSource.delete(setId)
                            navCoordinator.onBackPressed()
                        }
                    }
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
                        repChanged = { set = set.copy(reps = it) },
                        weightChanged = { set = set.copy(weight = it) },
                        rpeChanged = { set = set.copy(rpe = it) }
                    )

                    val sets = dependencies.database.setDataSource.getAllForLift(
                        variation?.lift?.id ?: ""
                    )

                    val liftMax = sets.maxByOrNull { it.weight }
                    val variationMax = sets.filter { it.variationId == variation?.id }
                        .maxByOrNull { it.weight }

                    AnimatedVisibility(set.variationId != null) {
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
                            Text("You got this!")
                        } else {
                            val sets = dependencies.database.setDataSource.getAllForLift(
                                variation.lift?.id ?: ""
                            )

                            val liftMax = sets.maxByOrNull { it.weight }
                            val variationMax = sets.filter { it.variationId == variation.id }
                                .maxByOrNull { it.weight }
                            Text("${variation.lift?.name ?: ""} Max: ${liftMax?.formattedWeight() ?: "None"}")
                            Text("${variation.fullName} Max: ${variationMax?.formattedWeight() ?: "None"}")
                        }
                    }
                }
            }

            item {
                TempoSelector(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
                    up = set.up?.toInt(),
                    hold = set.hold?.toInt(),
                    down = set.down?.toInt(),
                    downChanged = { set = set.copy(down = it?.toLong()) },
                    holdChanged = { set = set.copy(hold = it?.toLong()) },
                    upChanged = { set = set.copy(up = it?.toLong()) },
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

                DateSelector(
                    date = set.date,
                    dateChanged = { set = set.copy(date = it) }
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

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = set.notes,
                        singleLine = true,
                        placeholder = {
                            Text(stringResource(Res.string.edit_set_screen_extra_notes_placeholder))
                        },
                        onValueChange = {
                            set = set.copy(notes = it)
                        },
                    )
                }
            }
        }
    }

    VariationSearchDialog(
        visible = showVariationDialog,
        onDismissRequest = { showVariationDialog = false },
        variationSelected = {
            showVariationDialog = false
            set = set.copy(variationId = it.id)
        }
    )
}
