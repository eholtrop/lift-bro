package com.lift.bro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.set.RPE
import com.lift.bro.presentation.set.RpeInfoDialog
import com.lift.bro.ui.theme.aerospaceOrange
import com.lift.bro.ui.theme.amber
import com.lift.bro.ui.theme.orangePeel
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme

@Composable
fun RpeSelector(
    modifier: Modifier = Modifier,
    rpe: Int?,
    rpeChanged: (Int) -> Unit,
) {
    Row(
        modifier = modifier.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var showInfoDialog by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.clickable(
                role = Role.Button,
                onClick = {
                    showInfoDialog = true
                }
            )
                .defaultMinSize(minHeight = 48.dp, minWidth = 52.dp)
                .padding(
                    horizontal = MaterialTheme.spacing.half,
                    vertical = MaterialTheme.spacing.quarter,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = RPE.entries.firstOrNull { it.rpe == rpe }?.emoji ?: "RPE",
                style = MaterialTheme.typography.headlineSmall,
            )
            AnimatedVisibility(
                visible = rpe != null
            ) {
                rpe?.let {
                    Text("RPE $rpe", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        RpeBar(
            rpe = RPE.entries.firstOrNull { it.rpe == rpe },
            rpeChanged = { rpeChanged(it.rpe) }
        )

        if (showInfoDialog) {
            RpeInfoDialog(
                onDismissRequest = { showInfoDialog = false }
            )
        }
    }
}

@Composable
fun RpeBar(
    modifier: Modifier = Modifier,
    rpe: RPE?,
    rpeChanged: (RPE) -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        amber,
                        orangePeel,
                        aerospaceOrange
                    )
                ),
            ).fillMaxWidth()
                .height(MaterialTheme.spacing.quarter)
        ) {
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RPE.entries.forEach {
                val selected = it == rpe
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        modifier = Modifier.size(
                            if (selected) MaterialTheme.spacing.two else MaterialTheme.spacing.one
                        ),
                        onClick = {
                            rpeChanged(it)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (selected) {
                            Text(it.rpe.toString())
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RpeSelectorPreview(@PreviewParameter(DarkModeProvider::class) isDarkMode: Boolean) {
    PreviewAppTheme(
        isDarkMode = isDarkMode,
    ) {
        RpeSelector(
            rpe = RPE.Five.rpe,
            rpeChanged = {}
        )
    }
}
