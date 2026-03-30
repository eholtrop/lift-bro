package com.lift.bro.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Setting
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.presentation.LocalServer
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.settings.client.ClientSettingsRow
import com.lift.bro.presentation.settings.rows.LiftBroSettingsRow
import com.lift.bro.presentation.settings.server.ServerSettingsRow
import com.lift.bro.ui.CheckField
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.ReleaseNotesDialog
import com.lift.bro.ui.theme.spacing
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_footer_version
import lift_bro.core.generated.resources.privacy_policy
import lift_bro.core.generated.resources.settings_become_pro_description
import lift_bro.core.generated.resources.settings_become_pro_title
import lift_bro.core.generated.resources.settings_other_discord_cta
import lift_bro.core.generated.resources.settings_other_github_cta
import lift_bro.core.generated.resources.settings_pro_features_header
import lift_bro.core.generated.resources.settings_release_notes_cta
import lift_bro.core.generated.resources.settings_title
import lift_bro.core.generated.resources.settings_user_id_label
import lift_bro.core.generated.resources.terms_and_conditions
import lift_bro.core.generated.resources.url_privacy_policy
import lift_bro.core.generated.resources.url_terms_and_conditions
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    interactor: SettingsInteractor = rememberSettingsInteractor(),
) {
    val state by interactor.state.collectAsState()

    SettingsScreen(
        state = state,
        onEvent = { interactor(it) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    var showPaywall by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = { Text(stringResource(Res.string.settings_title)) },
        content = { padding ->

            var subscriptionType by LocalSubscriptionStatusProvider.current

            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
            ) {
                items(items = state.items) {
                    CardForSetting(it)
                }

                item {
                    SettingsRowItem(
                        title = {
                            Text("Experimental (expect bugs)")
                        }
                    ) {
                        state.experimental.forEach {
                            CardForSetting(it)
                        }
                    }
                }

                item {
                    Text(
                        modifier = Modifier.semantics {
                            heading()
                        },
                        text = stringResource(Res.string.settings_pro_features_header),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }

                if (state.proItems.isEmpty()) {
                    item {
                        when (subscriptionType) {
                            SubscriptionType.None -> {
                                SettingsRowItem(
                                    modifier = Modifier.clickable { showPaywall = true },
                                    title = { Text(stringResource(Res.string.settings_become_pro_title)) },
                                ) {
                                    Row {
                                        Text(stringResource(Res.string.settings_become_pro_description))
                                    }
                                    Text("Other Goodies (experimental features)")
                                }
                            }

                            else -> {}
                        }

                        // need to refresh payments whenever the paywal changes... hacky but works
                        // should abstract
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
                }

                items(items = state.proItems) {
                    CardForSetting(it)
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

                        var showReleaseNotesDialog by remember { mutableStateOf(false) }
                        if (showReleaseNotesDialog) {
                            ReleaseNotesDialog { showReleaseNotesDialog = false }
                        }
                        Button(
                            colors = ButtonDefaults.textButtonColors(),
                            onClick = {
                                showReleaseNotesDialog = true
                            }
                        ) {
                            Text(stringResource(Res.string.settings_release_notes_cta))
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
                    val coroutineScope = rememberCoroutineScope()
                    Text(
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                clipboard.setClipEntry(
                                    Purchases.sharedInstance.appUserID.toClipEntry()
                                )
                            }
                        },
                        text = stringResource(Res.string.settings_user_id_label, Purchases.sharedInstance.appUserID)
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
fun CardForSetting(
    item: SettingsItem,
) {
    when (item) {
        is SettingsItem.AppTheme -> ThemeSettingsRow()
        SettingsItem.Backup -> BackupSettingsRow()
        is SettingsItem.Bro -> LiftBroSettingsRow()
        is SettingsItem.DashboardV3 -> {
            Column {
                var dashboardV3 by remember {
                    mutableStateOf(
                        dependencies.settingsRepository.get(Setting.DashboardV3)
                    )
                }
                CheckField(
                    title = "Dashboard V3",
                    description = "No more tabs!",
                    checked = dashboardV3,
                    checkChanged = {
                        dashboardV3 = it
                        dependencies.settingsRepository.set(Setting.DashboardV3, it)
                    }
                )
            }
        }

        is SettingsItem.DbLocation -> ClientSettingsRow()
        is SettingsItem.MaxAppearance -> eMaxSettingsRow()
        is SettingsItem.MerSettings -> MERSettingsRow()
        is SettingsItem.ShowTWM -> TWMSettingsRow()
        is SettingsItem.Timer -> {
            var timer by remember {
                mutableStateOf(
                    dependencies.settingsRepository.get(Setting.TimerEnabled)
                )
            }
            CheckField(
                title = "Timer",
                description = "Enable a timer for counting down sets and rest periods",
                checked = timer,
                checkChanged = {
                    timer = it
                    dependencies.settingsRepository.set(Setting.TimerEnabled, it)
                }
            )
        }

        is SettingsItem.LocalServer -> LocalServer.current?.let { ServerSettingsRow(it) }
    }
}

expect fun String.toClipEntry(): ClipEntry
