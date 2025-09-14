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
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.theme.icons
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.prettyPrintSet


@Composable
fun SetInfoRow(
    modifier: Modifier = Modifier,
    set: LBSet,
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
                text = set.prettyPrintSet(),
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