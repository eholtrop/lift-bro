package com.lift.bro.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.BackupService
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.onboarding.LiftBro
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_empty_primary_cta_title
import lift_bro.core.generated.resources.dashboard_empty_secondary_cta_title
import lift_bro.core.generated.resources.dashboard_empty_title
import lift_bro.core.generated.resources.ic_lift_bro_leo
import lift_bro.core.generated.resources.ic_lift_bro_leo_concerned
import lift_bro.core.generated.resources.ic_lift_bro_leo_dark
import lift_bro.core.generated.resources.ic_lift_bro_leo_light
import lift_bro.core.generated.resources.ic_lift_bro_lisa
import lift_bro.core.generated.resources.ic_lift_bro_lisa_concerned
import lift_bro.core.generated.resources.ic_lift_bro_lisa_concerned_light
import lift_bro.core.generated.resources.ic_lift_bro_lisa_dark
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

fun LiftBro.iconRes(): DrawableResource {
    return when (this) {
        LiftBro.Leo -> Res.drawable.ic_lift_bro_leo
        LiftBro.Lisa -> Res.drawable.ic_lift_bro_lisa
    }
}

fun LiftBro.concernedIconRes(): DrawableResource {
    return when (this) {
        LiftBro.Leo -> Res.drawable.ic_lift_bro_leo_concerned
        LiftBro.Lisa -> Res.drawable.ic_lift_bro_lisa_concerned
    }
}

fun LiftBro.darkIconRes(): DrawableResource {
    return when (this) {
        LiftBro.Leo -> Res.drawable.ic_lift_bro_leo_dark
        LiftBro.Lisa -> Res.drawable.ic_lift_bro_lisa_dark
    }
}

fun LiftBro.lightIconRes(): DrawableResource {
    return when (this) {
        LiftBro.Leo -> Res.drawable.ic_lift_bro_leo_light
        LiftBro.Lisa -> Res.drawable.ic_lift_bro_lisa_concerned_light
    }
}

// TODO: need to setup back handling in onboarding
// continue adding branding throughout the app

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
        LocalLiftBro.current?.let {
            Image(
                modifier = Modifier.height(124.dp),
                painter = painterResource(it.iconRes()),
                contentDescription = "",
            )
        }
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
                text = stringResource(Res.string.dashboard_empty_primary_cta_title),
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.half))

        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                coroutineScope.launch {
                    BackupService.restore()
                }
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_empty_secondary_cta_title),
                )
            }
        }
    }
}