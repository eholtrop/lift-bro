package com.lift.bro.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.example.compose.ThemeMode
import com.lift.bro.BackupService
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.ui.DecimalPicker
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.NumberPicker
import com.lift.bro.ui.RadioField
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialog
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.decimalFormat
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen() {
    LiftingScaffold(
        title = "Settings",
        content = { padding ->

            var showExperimental by remember { mutableStateOf(false) }

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
                                .collectAsState(null)

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
                        title = { Text("Theme") }
                    ) {
                        val themeMode by dependencies.settingsRepository.getThemeMode()
                            .collectAsState(ThemeMode.System)
                        Row {
                            RadioField(
                                text = "Dark",
                                selected = themeMode == ThemeMode.Dark,
                                fieldSelected = {
                                    dependencies.settingsRepository.setThemeMode(ThemeMode.Dark)
                                }
                            )
                            RadioField(
                                text = "Light",
                                selected = themeMode == ThemeMode.Light,
                                fieldSelected = {
                                    dependencies.settingsRepository.setThemeMode(ThemeMode.Light)
                                }
                            )
                            RadioField(
                                text = "System",
                                selected = themeMode == ThemeMode.System,
                                fieldSelected = {
                                    dependencies.settingsRepository.setThemeMode(ThemeMode.System)
                                }
                            )
                        }
                    }
                }

                item {
                    SettingsRowItem(
                        title = { Text("Other:") },
                        content = {
                            Button(
                                colors = ButtonDefaults.textButtonColors(),
                                onClick = {
                                    dependencies.launchUrl("https://discord.gg/mgxQK8ma")
                                }
                            ) {
                                Text("Join the Discord! >")
                            }

                            Button(
                                colors = ButtonDefaults.textButtonColors(),
                                onClick = {
                                    dependencies.launchUrl("https://www.github.com/eholtrop/lift-bro")
                                }
                            ) {
                                Text("Source Code >")
                            }

//                            Button(
//                                colors = ButtonDefaults.textButtonColors(),
//                                onClick = {}
//                            ) {
//                                Text("Release Notes")
//                            }
                        }
                    )
                }

                item {
                    var value by remember { mutableStateOf("") }
                    if (showExperimental) {
                        Column {
                            Text(
                                text = "Experimental",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Features here are experimental, and could break app functionality, use with caution!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Experimental",
                                style = MaterialTheme.typography.titleLarge
                            )
                            TextField(
                                modifier = Modifier.fillParentMaxWidth(),
                                value = value,
                                onValueChange = {
                                    value = it
                                    if (value.toLowerCase(Locale.current) == "pizza") {
                                        showExperimental = true
                                    }
                                },
                                placeholder = { Text("What's the magic word?") }
                            )
                        }
                    }
                }
                if (showExperimental) {
                    item {
                        SettingsRowItem(
                            title = {
                                Text("Maximally Effective Reps (MERs)")
                            },
                            content = {
                                val showMerCalcs by dependencies.settingsRepository.getMerSettings()
                                    .collectAsState(MERSettings())

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Checkbox(
                                            checked = showMerCalcs.enabled,
                                            onCheckedChange = {
                                                dependencies.settingsRepository.setMerSettings(
                                                    showMerCalcs.copy(enabled = it)
                                                )
                                            }
                                        )

                                        Text("Show MER calculations")
                                    }

                                    Row {
                                        NumberPicker(
                                            modifier = Modifier.weight(1f),
                                            title = "Fatigue Threshold",
                                            selectedNum = (showMerCalcs.threshold * 100).toInt(),
                                            numberChanged = {
                                                dependencies.settingsRepository.setMerSettings(
                                                    showMerCalcs.copy(threshold = it?.toFloat() ?: 0f)
                                                )
                                            },
                                        )

                                        InfoDialogButton(
                                            dialogTitle = { Text("Fatigue Threshold") },
                                            dialogMessage = {
                                                Text("The % fatigue that you must hit of your max for a rep to be considered a MER (default 80%), This can be calculated by taking a look at your Max")
                                                Space(MaterialTheme.spacing.half)
                                                Text("ex: if you have a 100lbs max Bench Press and you do one rep at 80lbs. This is above the threshold of 80% and therefore an MER!")
                                            }
                                        )
                                    }

                                    Row {
                                        NumberPicker(
                                            modifier = Modifier.weight(1f),
                                            title = "Weekly MER Goal",
                                            selectedNum = showMerCalcs.weeklyTotalGoal,
                                            numberChanged = {
                                                dependencies.settingsRepository.setMerSettings(
                                                    showMerCalcs.copy(weeklyTotalGoal = it)
                                                )
                                            },
                                        )

                                        InfoDialogButton(
                                            dialogTitle = { Text("Weekly MER Goal") },
                                            dialogMessage = {
                                                Text("MER's can be used to track your progress in a week! Set yourself a weekly goal and celebrate your successes!")
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

