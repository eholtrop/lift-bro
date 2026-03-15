package com.lift.bro.presentation.settings.rows

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.Setting
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.home.iconRes
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_manage_subscription_cta
import lift_bro.core.generated.resources.settings_pro_status_text
import lift_bro.core.generated.resources.settings_pro_thanks_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LiftBroSettingsRow(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
        if (LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro) {
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
