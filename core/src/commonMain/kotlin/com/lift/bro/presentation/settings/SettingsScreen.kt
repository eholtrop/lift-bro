package com.lift.bro.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Settings
import com.lift.bro.presentation.components.RadioField
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.variation.UOM
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.utils.debug

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen() {
    LiftingScaffold(
        title = "Settings",
        content = { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one)
            ) {
                item {
                    val uom by dependencies.settingsRepository.getUnitOfMeasure()
                        .collectAsStateWithLifecycle(
                            null
                        )


                    Column(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                            )
                            .padding(MaterialTheme.spacing.one)
                    ) {
                        Text(
                            modifier = Modifier.semantics {
                                heading()
                            },
                            text = "Default Unit of Measure",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Space(MaterialTheme.spacing.quarter)

                        HorizontalDivider()

                        Space(MaterialTheme.spacing.half)

                        Row(
                            modifier = Modifier.selectableGroup(),
                        ) {
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
            }
        }
    )
}

