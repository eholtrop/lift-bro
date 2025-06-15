package com.lift.bro.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.lift.bro.BackupService
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Settings
import com.lift.bro.ui.RadioField
import com.lift.bro.ui.theme.spacing
import com.lift.bro.domain.models.UOM
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen() {
    LiftingScaffold(
        title = "Settings",
        content = { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
            ) {
                item {

                    SettingsRowItem(
                        title = { Text("Backup / Restore") }
                    ) {
                        Row(
                            modifier = Modifier.selectableGroup(),
                        ) {
                            val uom by dependencies.settingsRepository.getUnitOfMeasure()
                                .collectAsState(
                                    null
                                )

                            RadioField(
                                text = UOM.POUNDS.value,
                                selected = uom?.uom == UOM.POUNDS,
                                fieldSelected = {
                                    dependencies.settingsRepository.saveUnitOfMeasure(
                                        Settings.UnitOfWeight(
                                            UOM.POUNDS
                                        )
                                    )
                                }
                            )
                            Space(MaterialTheme.spacing.one)
                            RadioField(
                                text = UOM.KG.value,
                                selected = uom?.uom == UOM.KG,
                                fieldSelected = {
                                    dependencies.settingsRepository.saveUnitOfMeasure(
                                        Settings.UnitOfWeight(
                                            UOM.KG
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    BackupRow()
                }

                item {
                    SettingsRowItem(
                        title = {
                            Text("Maximally Effective Reps (MERs)")
                        },
                        content = {
                            val showMerCalcs by dependencies.settingsRepository.shouldShowMerCalcs().collectAsState(false)
                            Row {
                                Checkbox(
                                    checked = showMerCalcs,
                                    onCheckedChange = {
                                        dependencies.settingsRepository.setShowMerCalcs(it)
                                    }
                                )

                                Text("Show MER calculations where applicable")
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun BackupRow() {
    SettingsRowItem(
        title = { Text("Backup / Restore") }
    ) {
        val scope = rememberCoroutineScope()

        Row(
            modifier = Modifier.selectableGroup(),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    scope.launch {
                        BackupService.backup()
                    }
                }
            ) {
                Text("Backup")
            }

            Space(MaterialTheme.spacing.one)

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    scope.launch {
                        BackupService.restore()
                    }
                }
            ) {
                Text("Restore")
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {

    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.surface,
            )
            .padding(MaterialTheme.spacing.one)
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineSmall
        ) {
            title()
        }
        Space(MaterialTheme.spacing.quarter)
        HorizontalDivider()
        Space(MaterialTheme.spacing.quarter)
        content()
    }
}

