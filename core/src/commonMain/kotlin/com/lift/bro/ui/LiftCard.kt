package com.lift.bro.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toString
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.lift_card_empty_subtitle
import lift_bro.core.generated.resources.lift_card_empty_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.text.Typography.nbsp

data class LiftCardState(
    val lift: Lift,
    val values: List<Pair<LocalDate, LiftCardData>>,
)

data class LiftCardData(
    val weight: Double,
    val reps: Int,
    val rpe: Int?,
    private val offset: Offset = Offset.Zero,
)

enum class LiftCardYValue {
    Reps, Weight
}

@Composable
fun LiftCard(
    modifier: Modifier = Modifier,
    state: LiftCardState,
    onClick: (Lift) -> Unit,
    value: LiftCardYValue = LiftCardYValue.Weight
) {
    val lift = state.lift
    val max =
        if (value == LiftCardYValue.Reps) state.values.maxOfOrNull { it.second.reps.toDouble() } ?: 0.0 else state.lift.maxWeight
    val min = state.values.minOfOrNull {
        when (value) {
            LiftCardYValue.Reps -> 0.0
            LiftCardYValue.Weight -> it.second.weight
        }
    } ?: 0.0

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
                    style = MaterialTheme.typography.titleSmall,
                )
                Space()
                max?.let {
                    Text(
                        modifier = Modifier.wrapContentWidth(),
                        text = when (value) {
                            LiftCardYValue.Weight -> weightFormat(max)
                            LiftCardYValue.Reps -> "${max.toInt()}${nbsp}reps"
                        },
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            Space(MaterialTheme.spacing.half)

            if (max == null) {
                val coordinator = LocalNavCoordinator.current
                Column(
                    modifier = Modifier.fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            onClick = {
                                coordinator.present(Destination.EditSet(liftId = lift.id))
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
                        stringResource(Res.string.lift_card_empty_subtitle),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                val color = lift.color?.toColor() ?: MaterialTheme.colorScheme.onSurfaceVariant

                var canvasSize by remember { mutableStateOf(Size.Zero) }
                val animatedGraphNodes =
                    remember { mutableStateMapOf<LocalDate, Animatable<Offset, AnimationVector2D>>() }

                LaunchedEffect(value, canvasSize) {
                    if (canvasSize == Size.Zero) return@LaunchedEffect

                    val height = canvasSize.height
                    val width = canvasSize.width
                    val spacing = width.div(5)

                    val nodeData = when (value) {
                        LiftCardYValue.Reps -> state.values.map {
                            it.first to it.second.reps.toDouble()
                        }

                        LiftCardYValue.Weight -> state.values.map {
                            it.first to it.second.weight
                        }
                    }


                    nodeData.forEachIndexed { index, pair ->
                        val targetX = width - (spacing * index + spacing / 2)
                        val normalizedPercentage =
                            (pair.second - min * 0.95) / (max(1.0, max - min * 0.95))
                        val targetY = height - (normalizedPercentage * height).toFloat()
                        val newTargetOffset = Offset(targetX, targetY)

                        val animatable = animatedGraphNodes.getOrPut(pair.first) {
                            Animatable(Offset(targetX, height), Offset.VectorConverter)
                        }

                        launch {
                            animatable.animateTo(
                                targetValue = newTargetOffset,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                }



                Canvas(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    canvasSize = this.size
                    animatedGraphNodes.forEach { node ->
                        drawCircle(
                            color = color,
                            radius = 10.dp.value,
                            center = node.value.value
                        )
                    }
                }

                Space(MaterialTheme.spacing.half)

                Row {
                    Text(
                        text = state.values.maxOfOrNull { it.first }?.toString("MMM d") ?: "",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Space()
                    Text(
                        text = when (value) {
                            LiftCardYValue.Weight -> weightFormat(min)
                            LiftCardYValue.Reps -> "${min.toInt()} reps"
                        },
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun weightFormat(weight: Double): String {
    return "${weight.decimalFormat()} ${LocalUnitOfMeasure.current.value}"
}