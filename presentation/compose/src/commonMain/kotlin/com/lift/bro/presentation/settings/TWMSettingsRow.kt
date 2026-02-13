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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.config.BuildConfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_twm_enable_text
import lift_bro.core.generated.resources.settings_twm_fatigue_info_dialog_paragraph_one
import lift_bro.core.generated.resources.settings_twm_fatigue_info_dialog_title
import lift_bro.core.generated.resources.settings_twm_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun TWMSettingsRow() {
    val showTwm by dependencies.settingsRepository.shouldShowTotalWeightMoved()
        .collectAsState(false)

    TWMSettingsRowContent(
        enabled = showTwm,
        isPro = LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro || BuildConfig.isDebug,
        onToggle = { enabled ->
            dependencies.settingsRepository.showTotalWeightMoved(enabled)
        }
    )
}

@Composable
fun TWMSettingsRowContent(
    enabled: Boolean,
    isPro: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SettingsRowItem(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.settings_twm_title)
                )

                InfoDialogButton(
                    dialogTitle = { Text(stringResource(Res.string.settings_twm_fatigue_info_dialog_title)) },
                    dialogMessage = {
                        Column {
                            Text(stringResource(Res.string.settings_twm_fatigue_info_dialog_paragraph_one))
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
                        checked = enabled,
                        enabled = isPro,
                        onCheckedChange = onToggle
                    )

                    Text(stringResource(Res.string.settings_twm_enable_text))
                }
            }
        }
    )
}

@Preview
@Composable
fun TWMSettingsRowPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        TWMSettingsRowContent(
            enabled = true,
            isPro = true,
            onToggle = {}
        )
    }
}
