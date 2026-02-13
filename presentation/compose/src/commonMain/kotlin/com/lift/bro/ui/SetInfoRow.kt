package com.lift.bro.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.theme.icons
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.prettyPrintSet
import kotlinx.datetime.Clock

@Composable
fun SetInfoRow(
    modifier: Modifier = Modifier,
    set: LBSet,
    uom: UOM = LocalUnitOfMeasure.current,
    enableTwm: Boolean = LocalTwmSettings.current,
    enableMers: Boolean = LocalShowMERCalcs.current?.enabled == true,
    trailing: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = set.prettyPrintSet(
                    uom = uom,
                    enableTwm = enableTwm,
                    enableMers = enableMers,
                ),
                style = MaterialTheme.typography.titleMedium,
            )

            trailing()
        }
        set.tempo.render()
        if (set.notes.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = MaterialTheme.spacing.quarter),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(MaterialTheme.typography.labelSmall.fontSize.value.dp),
                    imageVector = MaterialTheme.icons.notes,
                    contentDescription = null,
                )
                Space(MaterialTheme.spacing.quarter)
                Text(
                    text = set.notes,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Preview
@Composable
fun SetInfoRowPreview(
    @PreviewParameter(
        DarkModeProvider::class
    ) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        // Set with notes
        SetInfoRow(
            set = LBSet(
                id = "1",
                reps = 5,
                weight = 135.0,
                rpe = 8,
                date = Clock.System.now(),
                tempo = Tempo(),
                notes = "Felt strong today!",
                variationId = "var-1"
            ),
            enableTwm = false,
            enableMers = false,
        )

        // Set without notes and twm
        SetInfoRow(
            set = LBSet(
                id = "2",
                reps = 6,
                weight = 100.0,
                rpe = 9,
                mer = 4,
                date = Clock.System.now(),
                tempo = Tempo(),
                notes = "multiline\nnotes",
                variationId = "var-1"
            ),
            enableTwm = true,
            enableMers = false,
        )

        // Set without notes and twm and mer
        SetInfoRow(
            set = LBSet(
                id = "2",
                reps = 8,
                weight = 225.0,
                rpe = 9,
                date = Clock.System.now(),
                tempo = Tempo(),
                notes = "",
                mer = 4,
                variationId = "var-1"
            ),
            enableTwm = true,
            enableMers = true,
        )

        // Bodyweight set
        SetInfoRow(
            set = LBSet(
                id = "3",
                reps = 10,
                weight = 25.0,
                rpe = 7,
                date = Clock.System.now(),
                tempo = Tempo(),
                variationId = "var-2",
                bodyWeightRep = true
            ),
            enableTwm = false,
            enableMers = true,
        )
    }
}
