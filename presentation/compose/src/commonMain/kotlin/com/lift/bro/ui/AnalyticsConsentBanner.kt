package com.lift.bro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lift.bro.ui.theme.spacing
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
        onClickLabel = "Enable Analytics",
    ) {
        Column(
            modifier = Modifier.padding(start = MaterialTheme.spacing.one),
        ) {
            Text(
                text = "Help Lift Bro improve!",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Share usage info (Screen Names, Button Clicks)",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "NO personal info",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Enable",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
