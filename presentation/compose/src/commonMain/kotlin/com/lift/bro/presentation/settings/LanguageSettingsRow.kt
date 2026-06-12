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
            Text("Language Settings")
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
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text("Select a Language")
                    },
                    text = {
                        Column {
                            SupportedLanguage.entries.forEach {
                                RadioField(
                                    text = it.languageName(),
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
