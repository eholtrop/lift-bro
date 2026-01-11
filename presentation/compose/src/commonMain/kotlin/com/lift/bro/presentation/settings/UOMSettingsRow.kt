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
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_uom_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun UOMSettingsRow() {
    val uom by dependencies.settingsRepository.getUnitOfMeasure()
        .collectAsState(null)

    UOMSettingsRowContent(
        selectedUOM = uom?.uom,
        onUOMSelected = { selectedUOM ->
            dependencies.settingsRepository.saveUnitOfMeasure(
                Settings.UnitOfWeight(selectedUOM)
            )
        }
    )
}

@Composable
fun UOMSettingsRowContent(
    selectedUOM: UOM?,
    onUOMSelected: (UOM) -> Unit
) {
    SettingsRowItem(
        title = { Text(stringResource(Res.string.settings_uom_title)) }
    ) {
        Row(
            modifier = Modifier.selectableGroup(),
        ) {
            RadioField(
                text = UOM.POUNDS.value,
                selected = selectedUOM == UOM.POUNDS,
                fieldSelected = { onUOMSelected(UOM.POUNDS) }
            )
            Space(MaterialTheme.spacing.one)
            RadioField(
                text = UOM.KG.value,
                selected = selectedUOM == UOM.KG,
                fieldSelected = { onUOMSelected(UOM.KG) }
            )
        }
    }
}

@Preview
@Composable
fun UOMSettingsRowPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        UOMSettingsRowContent(
            selectedUOM = UOM.POUNDS,
            onUOMSelected = {}
        )
    }
}
