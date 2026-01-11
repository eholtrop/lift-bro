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
import com.lift.bro.config.BuildConfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
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
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun eMaxSettingsRow() {
    val emaxEnabled by dependencies.settingsRepository.eMaxEnabled()
        .collectAsState(false)
    val tmaxEnabled by dependencies.settingsRepository.tMaxEnabled()
        .collectAsState(false)

    eMaxSettingsRowContent(
        eMaxEnabled = emaxEnabled,
        tMaxEnabled = tmaxEnabled,
        isPro = LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro || BuildConfig.isDebug,
        onEMaxToggle = { dependencies.settingsRepository.setEMaxEnabled(it) },
        onTMaxToggle = { dependencies.settingsRepository.setTMaxEnabled(it) }
    )
}

@Composable
fun eMaxSettingsRowContent(
    eMaxEnabled: Boolean,
    tMaxEnabled: Boolean,
    isPro: Boolean,
    onEMaxToggle: (Boolean) -> Unit,
    onTMaxToggle: (Boolean) -> Unit
) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = eMaxEnabled,
                        enabled = isPro,
                        onCheckedChange = onEMaxToggle
                    )

                    Text(stringResource(Res.string.settings_emax_enable_text))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = tMaxEnabled,
                        enabled = isPro,
                        onCheckedChange = onTMaxToggle
                    )

                    Text(stringResource(Res.string.settings_tmax_enable_text))
                }
            }
        }
    )
}

@Preview
@Composable
fun eMaxSettingsRowPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        eMaxSettingsRowContent(
            eMaxEnabled = true,
            tMaxEnabled = false,
            isPro = true,
            onEMaxToggle = {},
            onTMaxToggle = {}
        )
    }
}
