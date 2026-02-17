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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.lift.bro.config.BuildConfig
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalPlatformContext
import com.lift.bro.presentation.LocalServer
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.presentation.settings.client.ClientSettingsRow
import com.lift.bro.presentation.settings.server.ServerSettingsRow
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen() {
    var showPaywall by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = { Text(stringResource(Res.string.settings_title)) },
        content = { padding ->

            var subscriptionType by LocalSubscriptionStatusProvider.current
            var showExperimental by remember {
                mutableStateOf(
                    subscriptionType == SubscriptionType.Pro || BuildConfig.isDebug
                )
            }
            val localServer = LocalServer.current
            val showPro = LocalPlatformContext.current != null // (this means its iOS)

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
                                dependencies.settingsRepository.setBro(
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
                        }
                    }
                }

                item {
                    BackupSettingsRow()
                }

                item {
                    ThemeSettingsRow()
                }

                if (showPro) {
                    item {
                        Text(
                            modifier = Modifier.semantics {
                                heading()
                            },
                            text = stringResource(Res.string.settings_pro_features_header),
                            style = MaterialTheme.typography.headlineMedium,
                        )
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
                }

                if (showExperimental) {
                    if (localServer != null) {
                        item {
                            ServerSettingsRow(localServer)
                        }
                    }

                    item {
                        ClientSettingsRow()
                    }

                    item {
                        SettingsRowItem(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Timer (Experimental) - Expect \uD83E\uDD97")

                                    var enabled by remember {
                                        mutableStateOf(
                                            dependencies.settingsRepository.enableTimer()
                                        )
                                    }
                                    Checkbox(
                                        checked = enabled,
                                        onCheckedChange = {
                                            enabled = !enabled
                                            dependencies.settingsRepository.setEnableTimer(enabled)
                                        }
                                    )
                                }
                            },
                        ) {
                            Text(
                                "Enable a set timer/recorder for counting you down as well as record video of a given set"
                            )
                        }
                    }

                    item {
                        SettingsRowItem(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("AT Proto (Bluesky \uD83E\uDD8B) - Expect \uD83E\uDD97")

                                    var enabled by remember {
                                        mutableStateOf(
                                            dependencies.settingsRepository.enableATProto()
                                        )
                                    }
                                    Checkbox(
                                        checked = enabled,
                                        onCheckedChange = {
                                            enabled = !enabled
                                            dependencies.settingsRepository.setEnableATProto(enabled)
                                        }
                                    )
                                }
                            },
                        ) {
                            Text(
                                "Enable ATProto/BlueSky integration. Share your gains with other lifters!"
                            )
                        }
                    }
                }

                if (showPro || subscriptionType == SubscriptionType.Pro) {
                    item {
                        MERSettingsRow()
                    }

                    item {
                        TWMSettingsRow()
                    }

                    item {
                        eMaxSettingsRow()
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

expect fun String.toClipEntry(): ClipEntry
