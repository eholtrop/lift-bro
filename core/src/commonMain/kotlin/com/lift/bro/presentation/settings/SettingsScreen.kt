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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.example.compose.ThemeMode
import com.lift.bro.BackupService
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.NumberPicker
import com.lift.bro.ui.RadioField
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.privacy_policy
import lift_bro.core.generated.resources.settings_backup_cta
import lift_bro.core.generated.resources.settings_backup_restore_title
import lift_bro.core.generated.resources.settings_experimental_input_password
import lift_bro.core.generated.resources.settings_experimental_input_placeholder
import lift_bro.core.generated.resources.settings_experimental_message
import lift_bro.core.generated.resources.settings_experimental_title
import lift_bro.core.generated.resources.settings_mer_enable_text
import lift_bro.core.generated.resources.settings_mer_fatigue_info_dialog_paragraph_one
import lift_bro.core.generated.resources.settings_mer_fatigue_info_dialog_paragraph_two
import lift_bro.core.generated.resources.settings_mer_fatigue_info_dialog_title
import lift_bro.core.generated.resources.settings_mer_fatigue_input_title
import lift_bro.core.generated.resources.settings_mer_title
import lift_bro.core.generated.resources.settings_mer_weekly_goal_info_dialog_message
import lift_bro.core.generated.resources.settings_mer_weekly_goal_info_dialog_title
import lift_bro.core.generated.resources.settings_mer_weekly_goal_input_title
import lift_bro.core.generated.resources.settings_other_discord_cta
import lift_bro.core.generated.resources.settings_other_github_cta
import lift_bro.core.generated.resources.settings_other_title
import lift_bro.core.generated.resources.settings_restore_cta
import lift_bro.core.generated.resources.settings_theme_option_one
import lift_bro.core.generated.resources.settings_theme_option_three
import lift_bro.core.generated.resources.settings_theme_option_two
import lift_bro.core.generated.resources.settings_theme_title
import lift_bro.core.generated.resources.settings_title
import lift_bro.core.generated.resources.settings_uom_title
import lift_bro.core.generated.resources.terms_and_conditions
import lift_bro.core.generated.resources.url_privacy_policy
import lift_bro.core.generated.resources.url_terms_and_conditions
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen() {
    LiftingScaffold(
        title = stringResource(Res.string.settings_title),
        content = { padding ->

            var showExperimental by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
            ) {
                item {

                    SettingsRowItem(
                        title = { Text(stringResource(Res.string.settings_uom_title)) }
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
                        title = { Text(stringResource(Res.string.settings_theme_title)) }
                    ) {
                        val themeMode by dependencies.settingsRepository.getThemeMode()
                            .collectAsState(ThemeMode.System)
                        Row {
                            RadioField(
                                text = stringResource(Res.string.settings_theme_option_one),
                                selected = themeMode == ThemeMode.System,
                                fieldSelected = {
                                    dependencies.settingsRepository.setThemeMode(ThemeMode.System)
                                }
                            )
                            RadioField(
                                text = stringResource(Res.string.settings_theme_option_two),
                                selected = themeMode == ThemeMode.Light,
                                fieldSelected = {
                                    dependencies.settingsRepository.setThemeMode(ThemeMode.Light)
                                }
                            )
                            RadioField(
                                text = stringResource(Res.string.settings_theme_option_three),
                                selected = themeMode == ThemeMode.Dark,
                                fieldSelected = {
                                    dependencies.settingsRepository.setThemeMode(ThemeMode.Dark)
                                }
                            )
                        }
                    }
                }

                item {
                    var value by remember { mutableStateOf("") }
                    if (showExperimental) {
                        Column {
                            Text(
                                text = stringResource(Res.string.settings_experimental_title),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = stringResource(Res.string.settings_experimental_message),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = stringResource(Res.string.settings_experimental_title),
                                style = MaterialTheme.typography.titleLarge
                            )
                            val password =
                                stringResource(Res.string.settings_experimental_input_password)
                            TextField(
                                modifier = Modifier.fillParentMaxWidth(),
                                value = value,
                                onValueChange = {
                                    value = it
                                    if (value.toLowerCase(Locale.current) == password) {
                                        showExperimental = true
                                    }
                                },
                                placeholder = { Text(stringResource(Res.string.settings_experimental_input_placeholder)) }
                            )
                        }
                    }
                }
                if (showExperimental) {
                    item {
                        SettingsRowItem(
                            title = {
                                Text(stringResource(Res.string.settings_mer_title))
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

                                        Text(stringResource(Res.string.settings_mer_enable_text))
                                    }

                                    Row {
                                        NumberPicker(
                                            modifier = Modifier.weight(1f),
                                            title = stringResource(Res.string.settings_mer_fatigue_input_title),
                                            selectedNum = (showMerCalcs.threshold * 100).toInt(),
                                            numberChanged = {
                                                dependencies.settingsRepository.setMerSettings(
                                                    showMerCalcs.copy(
                                                        threshold = it?.toFloat() ?: 0f
                                                    )
                                                )
                                            },
                                        )

                                        InfoDialogButton(
                                            dialogTitle = { Text(stringResource(Res.string.settings_mer_fatigue_info_dialog_title)) },
                                            dialogMessage = {
                                                Text(stringResource(Res.string.settings_mer_fatigue_info_dialog_paragraph_one))
                                                Space(MaterialTheme.spacing.half)
                                                Text(stringResource(Res.string.settings_mer_fatigue_info_dialog_paragraph_two))
                                            }
                                        )
                                    }

                                    Row {
                                        NumberPicker(
                                            modifier = Modifier.weight(1f),
                                            title = stringResource(Res.string.settings_mer_weekly_goal_input_title),
                                            selectedNum = showMerCalcs.weeklyTotalGoal,
                                            numberChanged = {
                                                dependencies.settingsRepository.setMerSettings(
                                                    showMerCalcs.copy(weeklyTotalGoal = it)
                                                )
                                            },
                                        )

                                        InfoDialogButton(
                                            dialogTitle = { Text(stringResource(Res.string.settings_mer_weekly_goal_info_dialog_title)) },
                                            dialogMessage = {
                                                Text(stringResource(Res.string.settings_mer_weekly_goal_info_dialog_message))
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                item {
                    Column {
                        Button(
                            colors = ButtonDefaults.textButtonColors(),
                            onClick = {
                                dependencies.launchUrl("https://discord.gg/mgxQK8ma")
                            }
                        ) {
                            Text(stringResource(Res.string.settings_other_discord_cta))
                        }

                        Button(
                            colors = ButtonDefaults.textButtonColors(),
                            onClick = {
                                dependencies.launchUrl("https://www.github.com/eholtrop/lift-bro")
                            }
                        ) {
                            Text(stringResource(Res.string.settings_other_github_cta))
                        }
                        val terms = stringResource(Res.string.url_terms_and_conditions)
                        Button(
                            colors = ButtonDefaults.textButtonColors(),
                            onClick = {
                                dependencies.launchUrl(terms)
                            }
                        ) {
                            Text(stringResource(Res.string.terms_and_conditions))
                        }

                        val privacy = stringResource(Res.string.url_privacy_policy)
                        Button(
                            colors = ButtonDefaults.textButtonColors(),
                            onClick = {
                                dependencies.launchUrl(privacy)
                            }
                        ) {
                            Text(stringResource(Res.string.privacy_policy))
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun BackupRow() {
    SettingsRowItem(
        title = { Text(stringResource(Res.string.settings_backup_restore_title)) }
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
                Text(stringResource(Res.string.settings_backup_cta))
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
                Text(stringResource(Res.string.settings_restore_cta))
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

