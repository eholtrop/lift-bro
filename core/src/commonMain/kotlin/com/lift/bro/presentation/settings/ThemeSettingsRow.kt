package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.compose.ThemeMode
import com.lift.bro.di.dependencies
import com.lift.bro.ui.RadioField
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_theme_option_one
import lift_bro.core.generated.resources.settings_theme_option_three
import lift_bro.core.generated.resources.settings_theme_option_two
import lift_bro.core.generated.resources.settings_theme_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemeSettingsRow() {
    SettingsRowItem(
        title = { Text(stringResource(Res.string.settings_theme_title)) }
    ) {
        val themeMode by dependencies.settingsRepository.getThemeMode()
            .collectAsState(ThemeMode.System)
        Column {
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