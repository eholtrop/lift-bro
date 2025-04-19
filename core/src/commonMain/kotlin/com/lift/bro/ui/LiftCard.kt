package com.lift.bro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.decimalFormat
import com.lift.bro.presentation.dialog.CreateMaxSetDialog
import com.lift.bro.presentation.lift.toColor
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.theme.spacing
import com.lift.bro.presentation.toString
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.lift_card_empty_subtitle
import lift_bro.core.generated.resources.lift_card_empty_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LiftCard(
    modifier: Modifier = Modifier,
    lift: Lift,
    onClick: (Lift) -> Unit
) {

    val sets by
        dependencies.database.setDataSource.listenAllForLift(lift.id).collectAsState(
            emptyList()
        )

    val max = sets.maxByOrNull { it.weight }

    Card(
        modifier = modifier
            .aspectRatio(1f),
        onClick = { onClick(lift) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(all = MaterialTheme.spacing.half),
        ) {


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lift.name,
                    style = MaterialTheme.typography.labelLarge,
                )
                Space()
                max?.let {
                    Text(
                        text = max.weight.decimalFormat(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            Space(MaterialTheme.spacing.half)

            if (max == null) {
                var showMaxDialog by remember { mutableStateOf(false) }

                if (showMaxDialog) {
                    CreateMaxSetDialog(
                        parentLiftId = lift.id,
                        onDismissRequest = {
                            showMaxDialog = false
                        },
                        onSetCreated = {
                            showMaxDialog = false
                        },
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            onClick = {
                                showMaxDialog = true
                            },
                            role = Role.Button,
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(Res.string.lift_card_empty_title)
                    )
                    Text(
                        stringResource(Res.string.lift_card_empty_subtitle)
                    )
                }
            } else {
                val recentSets = sets.groupBy { it.date.toLocalDate() }
                    .map { Pair(it.key, it.value.maxBy { it.weight }) }
                    .sortedByDescending { it.first }
                    .take(5)

                val recentMin = recentSets.minOfOrNull { it.second.weight } ?: 0.0

                val color = lift.color?.toColor() ?: MaterialTheme.colorScheme.onSurfaceVariant
                Canvas(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    val height = this.size.height
                    val width = this.size.width
                    val spacing = width.div(5)


                    recentSets.forEachIndexed { index, lbSet ->
                        val x = width - (spacing * index + spacing / 2)
                        val normalizedPercentage = lbSet.second.weight.minus(recentMin.times(.95f))
                            .div(max?.weight?.minus(recentMin.times(.95f)) ?: 1.0)
                        val y = height - (normalizedPercentage) * height

                        drawCircle(
                            color = color,
                            radius = 10.dp.value,
                            center = Offset(x = x, y = y.toFloat())
                        )
                    }
                    drawLine(
                        color = color,
                        strokeWidth = 3f,
                        start = Offset(0f, height),
                        end = Offset(width, height)
                    )
                }

                Space(MaterialTheme.spacing.half)

                Row {
                    Text(
                        text = recentSets.maxOf { it.first }.toString("MMM d"),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Space()
                    Text(
                        text = recentMin.toString(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}