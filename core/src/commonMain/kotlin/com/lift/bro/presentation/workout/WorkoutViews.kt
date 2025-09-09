package com.lift.bro.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.workout_add_exercise_cta
import lift_bro.core.generated.resources.workout_add_finisher_cta
import lift_bro.core.generated.resources.workout_add_warmup_cta
import lift_bro.core.generated.resources.workout_finisher_label
import lift_bro.core.generated.resources.workout_finisher_placeholder
import lift_bro.core.generated.resources.workout_warmup_label
import lift_bro.core.generated.resources.workout_warmup_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun WarmupFinisherRow(
    warmup: String?,
    finisher: String?,
    eventHandler: (CreateWorkoutEvent) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.one)
    ) {
        if (warmup != null) {
            var warmup by remember { mutableStateOf(warmup) }
            TextField(
                modifier = Modifier.weight(1f),
                value = warmup,
                label = { Text(stringResource(Res.string.workout_warmup_label)) },
                placeholder = { Text(stringResource(Res.string.workout_warmup_placeholder)) },
                onValueChange = {
                    warmup = it
                    eventHandler(CreateWorkoutEvent.UpdateWarmup(it))
                },
            )
        } else {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    eventHandler(CreateWorkoutEvent.UpdateWarmup(""))
                },
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(stringResource(Res.string.workout_add_warmup_cta))
            }
        }

        Space(MaterialTheme.spacing.half)

        if (finisher != null) {
            var finisher by remember { mutableStateOf(finisher) }
            TextField(
                modifier = Modifier.weight(1f),
                value = finisher,
                label = { Text(stringResource(Res.string.workout_finisher_label)) },
                placeholder = { Text(stringResource(Res.string.workout_finisher_placeholder)) },
                onValueChange = {
                    finisher = it
                    eventHandler(CreateWorkoutEvent.UpdateFinisher(it))
                },
            )
        } else {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    eventHandler(CreateWorkoutEvent.UpdateFinisher(""))
                },
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(stringResource(Res.string.workout_add_finisher_cta))
            }
        }
    }
}

@Composable
fun AddExerciseRow(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.one)
            .defaultMinSize(minHeight = 52.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .clip(
                shape = MaterialTheme.shapes.medium
            )
            .clickable(
                onClick = onClick,
                role = Role.Button
            ).padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.half
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
        )
        Space(MaterialTheme.spacing.half)
        Text(
            stringResource(Res.string.workout_add_exercise_cta),
            style = MaterialTheme.typography.titleMedium
        )
    }
}