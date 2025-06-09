package com.lift.bro.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_empty_primary_cta_title
import lift_bro.core.generated.resources.dashboard_empty_secondary_cta_subtitle
import lift_bro.core.generated.resources.dashboard_empty_secondary_cta_title
import lift_bro.core.generated.resources.dashboard_empty_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EmptyHomeScreen(
    addLiftClicked: () -> Unit,
    loadDefaultLifts: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.dashboard_empty_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
        Button(
            onClick = addLiftClicked
        ) {
            Text(
                text = stringResource(Res.string.dashboard_empty_primary_cta_title),)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

        Button(
            onClick = loadDefaultLifts
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_empty_secondary_cta_title),
                )
                Text(
                    text = stringResource(Res.string.dashboard_empty_secondary_cta_subtitle),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}