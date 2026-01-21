package com.lift.bro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.set.TempoState
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.horizontal_padding.padding
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.tempo_selector_con_title
import lift_bro.core.generated.resources.tempo_selector_concentric_examples
import lift_bro.core.generated.resources.tempo_selector_concentric_subtitle
import lift_bro.core.generated.resources.tempo_selector_concentric_title
import lift_bro.core.generated.resources.tempo_selector_ecc_title
import lift_bro.core.generated.resources.tempo_selector_eccentric_examples
import lift_bro.core.generated.resources.tempo_selector_eccentric_subtitle
import lift_bro.core.generated.resources.tempo_selector_eccentric_title
import lift_bro.core.generated.resources.tempo_selector_iso_title
import lift_bro.core.generated.resources.tempo_selector_isometric_examples
import lift_bro.core.generated.resources.tempo_selector_isometric_subtitle
import lift_bro.core.generated.resources.tempo_selector_isometric_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun TempoSelector(
    modifier: Modifier = Modifier,
    tempo: TempoState,
    tempoChanged: (TempoState) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Row {
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = stringResource(Res.string.tempo_selector_ecc_title),
                selectedNum = tempo.ecc?.toInt(),
                numberChanged = { tempoChanged(tempo.copy(ecc = it?.toLong())) }
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = stringResource(Res.string.tempo_selector_iso_title),
                selectedNum = tempo.iso?.toInt(),
                numberChanged = { tempoChanged(tempo.copy(iso = it?.toLong())) }
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = stringResource(Res.string.tempo_selector_con_title),
                selectedNum = tempo.con?.toInt(),
                numberChanged = { tempoChanged(tempo.copy(con = it?.toLong())) }
            )
        }
    }
}

@Composable
fun TempoInfoDialogText() {
    Column {
        Text(
            text = stringResource(Res.string.tempo_selector_eccentric_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.tempo_selector_eccentric_subtitle),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = stringResource(Res.string.tempo_selector_eccentric_examples),
            style = MaterialTheme.typography.bodyMedium,
        )
        Space(MaterialTheme.spacing.one)
        Text(
            text = stringResource(Res.string.tempo_selector_isometric_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.tempo_selector_isometric_subtitle),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = stringResource(Res.string.tempo_selector_isometric_examples),
            style = MaterialTheme.typography.bodyMedium,
        )
        Space(MaterialTheme.spacing.one)
        Text(
            text = stringResource(Res.string.tempo_selector_concentric_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.tempo_selector_concentric_subtitle),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = stringResource(Res.string.tempo_selector_concentric_examples),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
fun TempoSelectorPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        TempoSelector(
            tempo = TempoState(
                ecc = 3,
                iso = 1,
                con = 1,
            ),
            tempoChanged = {}
        )

        TempoSelector(
            tempo = TempoState(
                ecc = 3,
                iso = null,
                con = 1,
            ),
            tempoChanged = {}
        )
    }
}
