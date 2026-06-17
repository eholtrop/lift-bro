package com.lift.bro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.ui.banner.DashboardBanner
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.analytics_consent_banner_onclick_label
import lift_bro.core.generated.resources.ai_translations_banner_subtitle
import lift_bro.core.generated.resources.ai_translations_banner_title
import lift_bro.core.generated.resources.ai_translations_dialog_body
import lift_bro.core.generated.resources.ai_translations_dialog_disable_radio
import lift_bro.core.generated.resources.ai_translations_dialog_enable_radio
import lift_bro.core.generated.resources.ai_translations_dialog_risk_title
import lift_bro.core.generated.resources.ai_translations_dialog_save_cta
import lift_bro.core.generated.resources.ai_translations_dialog_settings_note
import lift_bro.core.generated.resources.ai_translations_dialog_title
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.padding.horizontal.padding

@Composable
fun AITranslationsConsentBanner(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onEnable: () -> Unit,
    onDisableAI: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        var enableAiTranslations by remember { mutableStateOf<Boolean?>(null) }
        AlertDialog(
            title = { Text(stringResource(Res.string.ai_translations_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(Res.string.ai_translations_dialog_body),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Space(MaterialTheme.spacing.half)

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.ai_translations_dialog_risk_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )

                    Space(MaterialTheme.spacing.half)

                    Text(
                        stringResource(Res.string.ai_translations_dialog_settings_note),
                        style = MaterialTheme.typography.labelSmall
                    )

                    RadioField(
                        text = stringResource(Res.string.ai_translations_dialog_enable_radio),
                        selected = enableAiTranslations == true,
                        fieldSelected = {
                            enableAiTranslations = true
                        }
                    )

                    RadioField(
                        text = stringResource(Res.string.ai_translations_dialog_disable_radio),
                        selected = enableAiTranslations == false,
                        fieldSelected = {
                            enableAiTranslations = false
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (enableAiTranslations) {
                            true -> onEnable()
                            false -> onDisableAI()
                            null -> {}
                        }
                        showDialog = false
                    },
                    enabled = enableAiTranslations != null
                ) {
                    Text(stringResource(Res.string.ai_translations_dialog_save_cta))
                }
            },
            onDismissRequest = onDismiss
        )
    }

    DashboardBanner(
        modifier = modifier,
        onDismiss = onDismiss,
        onClick = {
            showDialog = true
        },
        onClickLabel = stringResource(Res.string.analytics_consent_banner_onclick_label),
    ) {
        Column(
            modifier = Modifier.padding(start = MaterialTheme.spacing.one),
        ) {
            Text(
                text = stringResource(Res.string.ai_translations_banner_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(Res.string.ai_translations_banner_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
