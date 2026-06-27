package com.lift.bro.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lift.bro.AppPurchases
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.repositories.Setting
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalServer
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.presentation.settings.client.ClientSettingsRow
import com.lift.bro.presentation.settings.server.ServerSettingsRow
import com.lift.bro.ui.CheckField
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.ReleaseNotesDialog
import com.lift.bro.ui.theme.spacing
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_footer_version
import lift_bro.core.generated.resources.privacy_policy
import lift_bro.core.generated.resources.settings_become_pro_description
import lift_bro.core.generated.resources.settings_become_pro_title
import lift_bro.core.generated.resources.settings_manage_subscription_cta
import lift_bro.core.generated.resources.settings_other_discord_cta
import lift_bro.core.generated.resources.settings_other_github_cta
import lift_bro.core.generated.resources.settings_pro_features_header
import lift_bro.core.generated.resources.settings_pro_status_text
import lift_bro.core.generated.resources.settings_pro_thanks_title
import lift_bro.core.generated.resources.settings_release_notes_cta
import lift_bro.core.generated.resources.settings_title
import lift_bro.core.generated.resources.settings_user_id_label
import lift_bro.core.generated.resources.terms_and_conditions
import lift_bro.core.generated.resources.url_privacy_policy
import lift_bro.core.generated.resources.url_terms_and_conditions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

enum class SettingsTab {
    Profile, Application
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    interactor: SettingsInteractor = rememberSettingsInteractor(),
) {
    val state by interactor.state.collectAsState()
    var showPaywall by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = { Text(stringResource(Res.string.settings_title)) },
        trailingContent = {
            var showReleaseNotesDialog by remember { mutableStateOf(false) }
            if (showReleaseNotesDialog) {
                ReleaseNotesDialog(
                    onDismissRequest = { showReleaseNotesDialog = false }
                )
            }
            IconButton(
                onClick = {
                    showReleaseNotesDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Notes,
                    contentDescription = "Release Notes"
                )
            }
        },
        content = { padding ->
            var subscriptionType by LocalSubscriptionStatusProvider.current
            val localServer = LocalServer.current

            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val bro = LocalLiftBro.current
                        IconButton(
                            modifier = Modifier.size(128.dp),
                            onClick = {
                                dependencies.settingsRepository.set(
                                    Setting.Bro,
                                    when (bro) {
                                        LiftBro.Leo -> LiftBro.Lisa
                                        LiftBro.Lisa -> LiftBro.Leo
                                    }
                                )
                            }
                        ) {
                            Image(
                                painter = painterResource(bro.iconRes()),
                                contentDescription = ""
                            )
                        }
                        if (subscriptionType == SubscriptionType.Pro) {
                            Text(
                                stringResource(Res.string.settings_pro_thanks_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                stringResource(Res.string.settings_pro_status_text),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(
                                onClick = {
                                    dependencies.launchManageSubscriptions()
                                },
                                colors = ButtonDefaults.textButtonColors()
                            ) {
                                Text(stringResource(Res.string.settings_manage_subscription_cta))
                            }
                        } else {
                            Row {
                                Text(
                                    text = buildAnnotatedString {
                                        append("Become a ")
                                        withLink(
                                            LinkAnnotation.Clickable(
                                                tag = "Lift Pro",
                                                styles = TextLinkStyles(
                                                    style = LocalTextStyle.current.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                    ).toSpanStyle()
                                                )
                                            ) {
                                                showPaywall = true

                                            }
                                        ) {
                                            append("Lift Pro >")
                                        }
                                    })
                            }
                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.labelSmall
                            ) {
                                Text("* Advanced Tracking Metrics")
                                Text("* Experimental Features")
                                Text("* Support Development")
                            }
                        }
                    }
                }

                stickyHeader {
                    state.selectedTab.let { tab ->
                        PrimaryTabRow(
                            containerColor = MaterialTheme.colorScheme.background,
                            selectedTabIndex = tab.ordinal
                        ) {
                            Tab(
                                selected = tab == SettingsTab.Profile,
                                onClick = {
                                    interactor(SettingsEvent.ProfileTabSelected)
                                },
                                text = { Text("Profile") }
                            )
                            Tab(
                                selected = tab == SettingsTab.Application,
                                onClick = {
                                    interactor(SettingsEvent.ApplicationTabSelected)
                                },
                                text = { Text("Application") }
                            )
                        }
                    }

                }

                items(state.settings) { item ->
                    SettingsRowLoader(item)
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

                items(state.proSettings) { item ->
                    SettingsRowLoader(item)
                }

                if (state.experimentalSettings.isNotEmpty()) {
                    item {
                        SettingsRowItem(
                            title = { Text("Experimental - Expect \uD83E\uDD97") }
                        ) {
                            Column {
                                state.experimentalSettings.forEach {
                                    SettingsRowLoader(it)
                                }

                                if (localServer != null) {
                                    ServerSettingsRow(localServer)
                                }
                            }
                        }
                    }
                }

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
                                    AppPurchases.appUserID.toClipEntry()
                                )
                            }
                        },
                        text = stringResource(Res.string.settings_user_id_label, AppPurchases.appUserID)
                    )
                }
            }

            // need to refresh payments whenever the paywal changes... hacky but works
            // should abstract
            LaunchedEffect(showPaywall) {
                AppPurchases.getCustomerInfo(
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
fun SettingsRowLoader(
    setting: Setting<*>,
) {
    when (setting) {
        Setting.AITranslationBannerDismissed -> TODO()
        Setting.AnalyticsConsent -> TODO()
        Setting.BackupSettings -> BackupSettingsRow()
        Setting.Bro -> TODO()
        Setting.ClientUrl -> ClientSettingsRow()
        Setting.Consent -> TODO()
        Setting.DashboardV3 -> {
            var dashboardV3 by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                dashboardV3 = dependencies.settingsRepository.get(Setting.DashboardV3)
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

        Setting.DeviceFtux -> TODO()
        Setting.EMaxEnabled -> eMaxSettingsRow()
        Setting.EditSetVersion -> TODO()
        Setting.LatestReadReleaseNotes -> TODO()
        Setting.LocaleOverride -> LanguageSettingsRow()
        Setting.MerSettings -> MERSettingsRow()
        Setting.ShowTotalWeightMoved -> TWMSettingsRow()
        Setting.TMaxEnabled -> eMaxSettingsRow()
        Setting.ThemeMode -> ThemeSettingsRow()
        Setting.Timer -> {
            var timer by remember {
                mutableStateOf(false)
            }
            LaunchedEffect(Unit) {
                timer = dependencies.settingsRepository.get(Setting.Timer)
            }

            CheckField(
                title = "Video Recording",
                description = "Record Videos and attach them to Sets to better understand your form!",
                checked = timer,
                checkChanged = {
                    timer = it
                    dependencies.settingsRepository.set(Setting.Timer, it)
                }
            )
        }

        Setting.UnitOfMeasure -> TODO()
    }
}

expect fun String.toClipEntry(): ClipEntry
