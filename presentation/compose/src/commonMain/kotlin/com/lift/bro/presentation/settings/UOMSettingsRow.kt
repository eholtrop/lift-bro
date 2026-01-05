package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.ui.RadioField
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_uom_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun UOMSettingsRow() {
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
