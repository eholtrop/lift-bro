package com.lift.bro.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.ClipMetadata
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.example.compose.ThemeMode
import com.lift.bro.BackupService
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.RadioField
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_footer_version
import lift_bro.core.generated.resources.privacy_policy
import lift_bro.core.generated.resources.settings_backup_cta
import lift_bro.core.generated.resources.settings_backup_restore_title
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
import lift_bro.core.generated.resources.settings_experimental_input_password
import lift_bro.core.generated.resources.settings_experimental_input_placeholder
import lift_bro.core.generated.resources.settings_experimental_message
import lift_bro.core.generated.resources.settings_experimental_title
import lift_bro.core.generated.resources.settings_mer_enable_text
import lift_bro.core.generated.resources.settings_mer_fatigue_info_dialog_paragraph_one
import lift_bro.core.generated.resources.settings_mer_fatigue_info_dialog_paragraph_two
import lift_bro.core.generated.resources.settings_mer_fatigue_info_dialog_title
import lift_bro.core.generated.resources.settings_mer_title
import lift_bro.core.generated.resources.settings_other_discord_cta
import lift_bro.core.generated.resources.settings_other_github_cta
import lift_bro.core.generated.resources.settings_restore_cta
import lift_bro.core.generated.resources.settings_theme_option_one
import lift_bro.core.generated.resources.settings_theme_option_three
import lift_bro.core.generated.resources.settings_theme_option_two
import lift_bro.core.generated.resources.settings_theme_title
import lift_bro.core.generated.resources.settings_title
import lift_bro.core.generated.resources.settings_tmax_enable_text
import lift_bro.core.generated.resources.settings_twm_enable_text
import lift_bro.core.generated.resources.settings_twm_fatigue_info_dialog_paragraph_one
import lift_bro.core.generated.resources.settings_twm_fatigue_info_dialog_title
import lift_bro.core.generated.resources.settings_twm_title
import lift_bro.core.generated.resources.settings_uom_title
import lift_bro.core.generated.resources.terms_and_conditions
import lift_bro.core.generated.resources.url_privacy_policy
import lift_bro.core.generated.resources.url_terms_and_conditions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen() {
    var showPaywall by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = { Text(stringResource(Res.string.settings_title)) },
        content = { padding ->

            var subscriptionType by LocalSubscriptionStatusProvider.current
            var showExperimental by remember { mutableStateOf(subscriptionType == SubscriptionType.Pro) }

            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
            ) {

                if (subscriptionType == SubscriptionType.Pro) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                modifier = Modifier.size(128.dp),
                                painter = painterResource(LocalLiftBro.current.iconRes()),
                                contentDescription = ""
                            )
                            Text(
                                "Thank you for the support!",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                "You are a Lift PRO!!",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(
                                onClick = {
                                    dependencies.launchManageSubscriptions()
                                },
                                colors = ButtonDefaults.textButtonColors()
                            ) {
                                Text("Manage Subscription")
                            }
                        }
                    }
                }

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
                        when (subscriptionType) {
                            SubscriptionType.None -> {
                                SettingsRowItem(
                                    modifier = Modifier.clickable { showPaywall = true },
                                    title = { Text("Become Pro!") },
                                ) {
                                    Row {
                                        Text("Sign up for an Ad free experience and extra premium tracking metrics!")
                                    }
                                }
                            }

                            else -> {}
                        }

                        LaunchedEffect(showPaywall) {
                            Purchases.sharedInstance.getCustomerInfo(
                                onError = { error ->
                                    Sentry.captureException(Throwable(message = error.message))
                                },
                                onSuccess = { success ->
                                    if (success.entitlements.active.containsKey("pro")) {
                                        subscriptionType = SubscriptionType.Pro
                                    }
                                }
                            )
                        }

                    }

                    item {
                        SettingsRowItem(
                            title = {
                                Row {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = stringResource(Res.string.settings_mer_title)
                                    )

                                    InfoDialogButton(
                                        dialogTitle = { Text(stringResource(Res.string.settings_mer_fatigue_info_dialog_title)) },
                                        dialogMessage = {
                                            Column {
                                                Text(stringResource(Res.string.settings_mer_fatigue_info_dialog_paragraph_one))
                                                Space(MaterialTheme.spacing.half)
                                                Text(stringResource(Res.string.settings_mer_fatigue_info_dialog_paragraph_two))
                                            }
                                        }
                                    )
                                }
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
                                            enabled = LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro,
                                            onCheckedChange = {
                                                dependencies.settingsRepository.setMerSettings(
                                                    showMerCalcs.copy(enabled = it)
                                                )
                                            }
                                        )

                                        Text(stringResource(Res.string.settings_mer_enable_text))
                                    }
                                }
                            }
                        )
                    }

                    item {
                        SettingsRowItem(
                            title = {
                                Row {
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
                                val showTwm by dependencies.settingsRepository.shouldShowTotalWeightMoved()
                                    .collectAsState(false)

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Checkbox(
                                            checked = showTwm,
                                            enabled = LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro,
                                            onCheckedChange = {
                                                dependencies.settingsRepository.showTotalWeightMoved(it)
                                            }
                                        )

                                        Text(stringResource(Res.string.settings_twm_enable_text))
                                    }
                                }
                            }
                        )
                    }

                    item {
                        SettingsRowItem(
                            title = {
                                Row {
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
                                    val emaxEnabled by dependencies.settingsRepository.eMaxEnabled().collectAsState(false)
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

                                    val tmaxEnabled by dependencies.settingsRepository.tMaxEnabled().collectAsState(false)
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

                item {
                    Text(
                        stringResource(
                            Res.string.dashboard_footer_version,
                            BuildKonfig.VERSION_NAME
                        )
                    )
                }

                item {
                    val clipboard = LocalClipboard.current
                    Text(
                        "User Id: ${Purchases.sharedInstance.appUserID}"
                    )
                }
            }
        }
    )

    val options = remember {
        PaywallOptions(dismissRequest = { showPaywall = false }) {
            shouldDisplayDismissButton = true
        }
    }


    AnimatedVisibility(
        visible = showPaywall,
        enter = slideInVertically { it },
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Paywall(options)
    }
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
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
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

