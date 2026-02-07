package com.lift.bro.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.ui.RadioField
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme

@Composable
fun SettingsRowItem(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
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

@Preview
@Composable
fun SettingsRowItemPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            SettingsRowItem(
                title = { Text("Unit of Measure") },
                content = {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        RadioField(
                            text = "lbs",
                            selected = true,
                            fieldSelected = {}
                        )
                        Space(MaterialTheme.spacing.one)
                        RadioField(
                            text = "kg",
                            selected = false,
                            fieldSelected = {}
                        )
                    }
                }
            )

            SettingsRowItem(
                title = { Text("Theme") },
                content = {
                    Column {
                        RadioField(
                            text = "System",
                            selected = true,
                            fieldSelected = {}
                        )
                        RadioField(
                            text = "Light",
                            selected = false,
                            fieldSelected = {}
                        )
                        RadioField(
                            text = "Dark",
                            selected = false,
                            fieldSelected = {}
                        )
                    }
                }
            )
        }
    }
}
