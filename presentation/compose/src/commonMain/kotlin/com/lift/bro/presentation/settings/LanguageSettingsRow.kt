package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.di.dependencies
import com.lift.bro.domain.repositories.Setting
import com.lift.bro.presentation.LocalLocale
import com.lift.bro.ui.RadioField
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.backup_dialog_cancel_cta
import lift_bro.core.generated.resources.language_settings_dialog_title
import lift_bro.core.generated.resources.language_settings_save_cta
import lift_bro.core.generated.resources.language_settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SupportedLanguage.languageName() = when (this) {
    SupportedLanguage.English -> "English"
    SupportedLanguage.Portugease -> "Portugease"
    SupportedLanguage.French -> "French"
    SupportedLanguage.Spanish -> "Spanish"
}

fun String.supportedLanguage(): SupportedLanguage? = when (this) {
    "en" -> SupportedLanguage.English
    "fr" -> SupportedLanguage.French
    "pt" -> SupportedLanguage.Portugease
    "es" -> SupportedLanguage.Spanish
    else -> null
}

fun SupportedLanguage.languageTag(): String = when (this) {
    SupportedLanguage.English -> "en"
    SupportedLanguage.French -> "fr"
    SupportedLanguage.Portugease -> "pt"
    SupportedLanguage.Spanish -> "es"
}

fun SupportedLanguage.isAiGen(): Boolean = when (this) {
    SupportedLanguage.English -> false
    else -> true
}

enum class SupportedLanguage {
    English,
    French,
    Portugease,
    Spanish,
}

@Composable
fun LanguageSettingsRow() {
    SettingsRowItem(
        title = {
            Text(stringResource(Res.string.language_settings_title))
        },
        content = {
            var showDialog by remember { mutableStateOf(false) }
            var selectedLanguage by remember {
                mutableStateOf(
                    LocalLocale.current.supportedLanguage() ?: SupportedLanguage.English
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDialog = true },
            ) {
                Text(selectedLanguage.languageName())
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                dependencies.settingsRepository.set(
                                    Setting.LocaleOverride,
                                    selectedLanguage.languageTag()
                                )
                                showDialog = false
                            }
                        ) {
                            Text(stringResource(Res.string.language_settings_save_cta))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showDialog = false
                            }
                        ) {
                            Text(stringResource(Res.string.backup_dialog_cancel_cta))
                        }
                    },
                    title = {
                        Text(stringResource(Res.string.language_settings_dialog_title))
                    },
                    text = {
                        Column {
                            SupportedLanguage.entries.forEach {
                                RadioField(
                                    text = "${it.languageName()}${if (it.isAiGen()) " (AI Gen)" else ""}",
                                    selected = selectedLanguage == it,
                                    fieldSelected = {
                                        selectedLanguage = it
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    )
}

@Preview
@Composable
fun LanguageSettingsRowPreview(@PreviewParameter(DarkModeProvider::class) isDarkMode: Boolean) {
    PreviewAppTheme(isDarkMode = isDarkMode) {
        LanguageSettingsRow()
    }
}
