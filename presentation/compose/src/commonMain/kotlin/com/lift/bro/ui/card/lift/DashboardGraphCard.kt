package com.lift.bro.ui.card.lift

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.dashboard.DashboardListItem
import com.lift.bro.ui.Card
import com.lift.bro.ui.FloatVectorConverter
import com.lift.bro.ui.Space
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.decimalFormat
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.lift_card_empty_subtitle
import lift_bro.core.generated.resources.lift_card_empty_title
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.toColor
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.ktx.datetime.toLocalDate
import tv.dpal.navi.LocalNavCoordinator
import kotlin.math.max
import kotlin.text.Typography.nbsp

private const val GRADIENT_SIZE = 50f


@Composable
fun DashboardGraphCard(
    modifier: Modifier = Modifier,
    state: DashboardListItem.GraphCard,
    onClick: (String) -> Unit,
    showRpe: Boolean = true,
    showTempo: Boolean = true,
    yUnit: LiftCardYValue = LiftCardYValue.Weight,
) {
    val min = state.min
    val max = state.max

    Card(
        modifier = modifier
            .aspectRatio(1f),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                Color.Transparent,
            ),
        ),
        contentPadding = PaddingValues(0.dp),
        onClick = { onClick(state.filterId) }
    ) {
//        state.lift.color?.toColor()?.let {
//            Box(
//                modifier = Modifier.fillMaxSize().background(
//                    brush = Brush.linearGradient(
//                        colors = listOf(
//                            it,
//                            Color.Transparent,
//                        ),
//                        end = Offset(
//                            GRADIENT_SIZE,
//                            GRADIENT_SIZE
//                        )
//                    )
//                )
//            ) {
//            }
//        }
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(all = MaterialTheme.spacing.one),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = state.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Space(width = MaterialTheme.spacing.half)
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = when (yUnit) {
                        LiftCardYValue.Weight -> weightFormat(weight = max?.value?.weight ?: 0.0)
                        LiftCardYValue.Reps -> "${max?.value?.weight?.toInt()}${nbsp}reps"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.MiddleEllipsis,
                )
            }

            Space(MaterialTheme.spacing.half)


            var canvasSize by remember { mutableStateOf(Size.Zero) }
            val animatedGraphNodes =
                remember {
                    mutableStateMapOf<
                        LocalDate,
                        Triple<
                            Animatable<Offset, AnimationVector2D>,
                            AnimatableFloat,
                            Triple<AnimatableFloat, AnimatableFloat, AnimatableFloat>
                            >
                        >()
                }

            LaunchedEffect(
                yUnit,
                canvasSize,
                state.data,
                showRpe,
                showTempo
            ) {
                val height = canvasSize.height
                val width = canvasSize.width

                val nodeData = state.data

                nodeData.forEachIndexed { index, node ->
                    val targetX = width / 2f
                    val normalizedPercentage =
                        (node.value.weight - (min?.value?.weight ?: 0.0) * 0.95) / (max(1.0, (max?.value?.weight ?: 0.0) - (min?.value?.weight ?: 0.0) * 0.95))
                    val targetY = height - (normalizedPercentage * height).toFloat()
                    val newTargetOffset = Offset(targetX, targetY)

                    val animatablePair = animatedGraphNodes.getOrPut(node.value.date.toLocalDate()) {
                        Triple(
                            Animatable(
                                Offset(targetX, targetY),
                                Offset.VectorConverter
                            ),
                            Animatable(0f, FloatVectorConverter),
                            Triple(
                                Animatable(0f, FloatVectorConverter),
                                Animatable(0f, FloatVectorConverter),
                                Animatable(0f, FloatVectorConverter),
                            ),
                        )
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
                            targetValue = if (showRpe) {
                                canvasSize.height * (node.value.rpe?.div(10f) ?: 0f)
                            } else {
                                0f
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }

                    launch {
                        animatablePair.third.first.animateTo(
                            targetValue = if (showTempo) node.value.tempo.down.toFloat() else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    launch {
                        animatablePair.third.second.animateTo(
                            targetValue = if (showTempo) node.value.tempo.hold.toFloat() else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    launch {
                        animatablePair.third.third.animateTo(
                            targetValue = if (showTempo) node.value.tempo.up.toFloat() else 0f,
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

                val nodes = animatedGraphNodes.toList().sortedBy { it.first }

                nodes.forEachIndexed { index, node ->
                    val previousNode = nodes.getOrNull(index - 1)
                    val nextNode = nodes.getOrNull(index + 1)

                    val color = MaterialTheme.colorScheme.onSurfaceVariant
                    Canvas(
                        modifier = Modifier.fillMaxHeight().weight(1f),
                    ) {
                        canvasSize = size
                        drawCircle(
                            color = color,
                            radius = 10.dp.value,
                            center = node.second.first.value
                        )

                        previousNode?.let {
                            drawLine(
                                color = color,
                                start = previousNode.second.first.value.copy(
                                    x = 0f,
                                    y = (previousNode.second.first.value.y + node.second.first.value.y) / 2
                                ),
                                end = node.second.first.value
                            )
                        }

                        nextNode?.let {
                            drawLine(
                                color = color,
                                start = node.second.first.value,
                                end = nextNode.second.first.value.copy(
                                    x = size.width,
                                    y = (nextNode.second.first.value.y + node.second.first.value.y) / 2
                                )
                            )
                        }

                        // Draw RPE
                        val rpe = node.second.second
                        if (rpe.value != 0f) {
                            drawRect(
                                color = color,
                                size = Size(size.width, 4.dp.value),
                                topLeft = Offset(
                                    x = 0f,
                                    y = size.height - (rpe.targetValue)
                                ),
                                alpha = rpe.value / rpe.targetValue
                            )
                            drawRect(
                                color = color.copy(alpha = .4f),
                                size = Size(size.width, rpe.value),
                                topLeft = Offset(
                                    x = 0f,
                                    y = size.height - (rpe.value)
                                )
                            )
                        }

                        // Draw Tempo
                        val (ecc, iso, con) = node.second.third

                        val max = listOf(5f, ecc.value, iso.value, con.value).max()
                        val width = size.width / 3

                        with(ecc) {
                            drawGraphBar(
                                color = color,
                                height = size.height * (value / max),
                                targetHeight = size.height * (targetValue / max),
                                width = width,
                                topLeft = 0f,
                                alpha = if (value <= targetValue) value / targetValue else 0f,
                            )
                        }

                        with(iso) {
                            drawGraphBar(
                                color = color,
                                height = size.height * (value / max),
                                targetHeight = size.height * (targetValue / max),
                                width = width,
                                topLeft = width,
                                alpha = if (value <= targetValue) value / targetValue else 0f,
                            )
                        }

                        with(con) {
                            drawGraphBar(
                                color = color,
                                height = size.height * (value / max),
                                targetHeight = size.height * (targetValue / max),
                                width = width,
                                topLeft = width * 2,
                                alpha = if (value <= targetValue) value / targetValue else 0f,
                            )
                        }
                    }
                }
            }

            Space(MaterialTheme.spacing.half)

            Row {
                Text(
                    text = state.data.maxOfOrNull { it.value.date }?.toString("MMM d") ?: "",
                    style = MaterialTheme.typography.labelMedium,
                )
                Space()
                Text(
                    text = when (yUnit) {
                        LiftCardYValue.Weight -> weightFormat(min?.value?.weight ?: 0.0)
                        LiftCardYValue.Reps -> "${min?.value?.reps?.toInt()} reps"
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

private fun DrawScope.drawGraphBar(
    color: Color,
    height: Float,
    targetHeight: Float,
    width: Float,
    topLeft: Float,
    alpha: Float,
) {
    drawRect(
        color = color,
        size = Size(width, 4.dp.value),
        topLeft = Offset(
            x = topLeft,
            y = size.height - targetHeight
        ),
        alpha = alpha
    )
    drawRect(
        color = color.copy(alpha = .4f),
        size = Size(
            width = width,
            height = height
        ),
        topLeft = Offset(
            x = topLeft,
            y = size.height - height,
        )
    )
}
