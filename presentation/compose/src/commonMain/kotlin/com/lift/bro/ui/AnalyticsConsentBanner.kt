package com.lift.bro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.analytics_consent_banner_action
import lift_bro.core.generated.resources.analytics_consent_banner_detail
import lift_bro.core.generated.resources.analytics_consent_banner_onclick_label
import lift_bro.core.generated.resources.analytics_consent_banner_privacy
import lift_bro.core.generated.resources.analytics_consent_banner_title
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.padding.horizontal.padding

@Composable
fun AnalyticsConsentBanner(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onEnable: () -> Unit,
) {
    com.lift.bro.ui.banner.DashboardBanner(
        modifier = modifier,
        onDismiss = onDismiss,
        onClick = onEnable,
        onClickLabel = stringResource(Res.string.analytics_consent_banner_onclick_label),
    ) {
        Column(
            modifier = Modifier.padding(start = MaterialTheme.spacing.one),
        ) {
            Text(
                text = stringResource(Res.string.analytics_consent_banner_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(Res.string.analytics_consent_banner_detail),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(Res.string.analytics_consent_banner_privacy),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(Res.string.analytics_consent_banner_action),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
