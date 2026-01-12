package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.ui.RadioField
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_theme_option_one
import lift_bro.core.generated.resources.settings_theme_option_three
import lift_bro.core.generated.resources.settings_theme_option_two
import lift_bro.core.generated.resources.settings_theme_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun ThemeSettingsRow() {
    val themeMode by dependencies.settingsRepository.getThemeMode()
        .collectAsState(ThemeMode.System)

    ThemeSettingsRowContent(
        selectedTheme = themeMode,
        onThemeSelected = { theme ->
            dependencies.settingsRepository.setThemeMode(theme)
        }
    )
}

@Composable
fun ThemeSettingsRowContent(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    SettingsRowItem(
        title = { Text(stringResource(Res.string.settings_theme_title)) }
    ) {
        Column {
            RadioField(
                text = stringResource(Res.string.settings_theme_option_one),
                selected = selectedTheme == ThemeMode.System,
                fieldSelected = { onThemeSelected(ThemeMode.System) }
            )
            RadioField(
                text = stringResource(Res.string.settings_theme_option_two),
                selected = selectedTheme == ThemeMode.Light,
                fieldSelected = { onThemeSelected(ThemeMode.Light) }
            )
            RadioField(
                text = stringResource(Res.string.settings_theme_option_three),
                selected = selectedTheme == ThemeMode.Dark,
                fieldSelected = { onThemeSelected(ThemeMode.Dark) }
            )
        }
    }
}

@Preview
@Composable
fun ThemeSettingsRowPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        ThemeSettingsRowContent(
            selectedTheme = ThemeMode.System,
            onThemeSelected = {}
        )
    }
}
