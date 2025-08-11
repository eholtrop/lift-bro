package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_emax_enable_text
import lift_bro.core.generated.resources.settings_emax_formula
import lift_bro.core.generated.resources.settings_emax_info_dialog_h1
import lift_bro.core.generated.resources.settings_emax_info_dialog_h2
import lift_bro.core.generated.resources.settings_emax_info_dialog_p1
import lift_bro.core.generated.resources.settings_emax_info_dialog_p1_ex
import lift_bro.core.generated.resources.settings_emax_info_dialog_p2
import lift_bro.core.generated.resources.settings_emax_info_dialog_p2_ex
import lift_bro.core.generated.resources.settings_emax_info_dialog_title
import lift_bro.core.generated.resources.settings_emax_title
import lift_bro.core.generated.resources.settings_tmax_enable_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun eMaxSettingsRow() {
    SettingsRowItem(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.settings_emax_title)
                )

                InfoDialogButton(
                    dialogTitle = { Text(stringResource(Res.string.settings_emax_info_dialog_title)) },
                    dialogMessage = {
                        Column {

                            Text(
                                stringResource(Res.string.settings_emax_formula),
                                style = MaterialTheme.typography.titleLarge,
                            )


                            Text(
                                stringResource(Res.string.settings_emax_info_dialog_h1),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(Res.string.settings_emax_info_dialog_p1),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Space(MaterialTheme.spacing.one)
                            Text(
                                stringResource(Res.string.settings_emax_info_dialog_p1_ex),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Space(MaterialTheme.spacing.one)
                            Text(
                                stringResource(Res.string.settings_emax_info_dialog_h2),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                stringResource(Res.string.settings_emax_info_dialog_p2),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Space(MaterialTheme.spacing.one)
                            Text(
                                stringResource(Res.string.settings_emax_info_dialog_p2_ex),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                )
            }
        },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
            ) {
                val emaxEnabled by dependencies.settingsRepository.eMaxEnabled()
                    .collectAsState(false)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = emaxEnabled,
                        enabled = LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro,
                        onCheckedChange = {
                            dependencies.settingsRepository.setEMaxEnabled(it)
                        }
                    )

                    Text(stringResource(Res.string.settings_emax_enable_text))
                }

                val tmaxEnabled by dependencies.settingsRepository.tMaxEnabled()
                    .collectAsState(false)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = tmaxEnabled,
                        enabled = LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro,
                        onCheckedChange = {
                            dependencies.settingsRepository.setTMaxEnabled(it)
                        }
                    )

                    Text(stringResource(Res.string.settings_tmax_enable_text))
                }
            }
        }
    )
}