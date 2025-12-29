package com.lift.bro.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toString
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.lift_card_empty_subtitle
import lift_bro.core.generated.resources.lift_card_empty_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.text.Typography.nbsp


@Serializable
data class LiftCardState(
    val lift: Lift,
    val values: List<Pair<LocalDate, LiftCardData>>,
    val maxWeight: Double? = null,
    val maxReps: Double? = null,
)


@Serializable
data class LiftCardData(
    val weight: Double,
    val reps: Int,
    val rpe: Int?,
    private val offset: LBOffset = LBOffset(),
)


@Serializable
data class LBOffset(
    val x: Float = 0f,
    val y: Float = 0f,
)

enum class LiftCardYValue {
    Reps, Weight
}

@Composable
fun LiftCard(
    modifier: Modifier = Modifier,
    state: LiftCardState,
    onClick: (Lift) -> Unit,
    value: LiftCardYValue = LiftCardYValue.Weight,
) {
    val lift = state.lift
    val max = if (value == LiftCardYValue.Reps) state.maxReps ?: 0.0 else state.maxWeight ?: 0.0
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
                    modifier = Modifier.weight(1f),
                    text = lift.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Space(width = MaterialTheme.spacing.half)
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = when (value) {
                        LiftCardYValue.Weight -> weightFormat(max)
                        LiftCardYValue.Reps -> "${max.toInt()}${nbsp}reps"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.MiddleEllipsis,
                )
            }

            Space(MaterialTheme.spacing.half)

            if (max == null) {
                val coordinator = LocalNavCoordinator.current
                Column(
                    modifier = Modifier.fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            onClick = {
                                coordinator.present(Destination.CreateSet(liftId = lift.id))
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
                    remember { mutableStateMapOf<LocalDate, Pair<Animatable<Offset, AnimationVector2D>, Animatable<Float, AnimationVector1D>?>>() }

                LaunchedEffect(value, canvasSize, state.values) {
                    val height = canvasSize.height
                    val width = canvasSize.width
                    val spacing = width.div(5)

                    val nodeData = when (value) {
                        LiftCardYValue.Reps -> state.values.map {
                            it.first to Pair(it.second.reps.toDouble(), it.second.rpe)
                        }

                        LiftCardYValue.Weight -> state.values.map {
                            it.first to Pair(it.second.weight, it.second.rpe)
                        }
                    }

                    nodeData.forEachIndexed { index, pair ->
                        val targetX = width / 2f
                        val normalizedPercentage =
                            (pair.second.first - min * 0.95) / (max(1.0, max - min * 0.95))
                        val targetY = height - (normalizedPercentage * height).toFloat()
                        val newTargetOffset = Offset(targetX, targetY)

                        val animatablePair = animatedGraphNodes.getOrPut(pair.first) {
                            Animatable(
                                Offset(targetX, targetY),
                                Offset.VectorConverter
                            ) to Animatable(0f, FloatVectorConverter)
                        }

                        launch {
                            animatablePair.first.animateTo(
                                targetValue = newTargetOffset,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }

                        launch {
                            animatablePair.second?.animateTo(
                                targetValue = canvasSize.height * (pair.second.second?.div(10f)
                                    ?: 0f),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    if (animatedGraphNodes.size < 5) {
                        Box(modifier = Modifier.weight(5f - animatedGraphNodes.size))
                    }

                    animatedGraphNodes.toList().sortedBy { it.first }
                        .forEachIndexed { index, node ->
                            Canvas(
                                modifier = Modifier.fillMaxHeight().weight(1f),
                            ) {
                                canvasSize = size
                                drawCircle(
                                    color = color,
                                    radius = 10.dp.value,
                                    center = node.second.first.value
                                )
                                if (node.second.second?.value != null) {
                                    with(node.second.second?.value) {
                                        if (this != 0f && this != null) {
                                            drawRect(
                                                color = color,
                                                size = Size(size.width, 4.dp.value),
                                                topLeft = Offset(
                                                    x = 0f,
                                                    y = size.height - (this)
                                                )
                                            )
                                            drawRect(
                                                color = color.copy(alpha = .4f),
                                                size = Size(size.width, this),
                                                topLeft = Offset(
                                                    x = 0f,
                                                    y = size.height - (this)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
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
fun weightFormat(weight: Double, uom: UOM = LocalUnitOfMeasure.current, useGrouping: Boolean = false): String {
    return "${weight.decimalFormat(grouping = useGrouping)} ${uom.value}"
}

@Composable
fun setFormat(
    weight: Double,
    reps: Int,
    bodyWeight: Boolean = false,
    useGrouping: Boolean = false,
): AnnotatedString = buildAnnotatedString {
    withStyle(
        style = SpanStyle(),
    ) {
        if (bodyWeight) {
            append("$reps x bw")
            if (weight > 0.0) {
                append(" + ${weightFormat(weight, useGrouping = useGrouping)}")
            }
        } else {
            append("$reps x ${weightFormat(weight, useGrouping = useGrouping)}")
        }
    }
}
