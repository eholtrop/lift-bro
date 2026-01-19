package com.lift.bro.ui

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
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
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
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
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

private const val GRADIENT_SIZE = 50f

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
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                Color.Transparent,
            ),
        ),
        contentPadding = PaddingValues(0.dp),
        onClick = { onClick(lift) }
    ) {
        state.lift.color?.toColor()?.let {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            it,
                            Color.Transparent,
                        ),
                        end = Offset(
                            GRADIENT_SIZE,
                            GRADIENT_SIZE
                        )
                    )
                )
            ) {

            }
        }
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(all = MaterialTheme.spacing.one),
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
                    remember {
                        mutableStateMapOf<LocalDate, Pair<Animatable<Offset, AnimationVector2D>, Animatable<Float, AnimationVector1D>?>>()
                    }

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
                                targetValue = canvasSize.height * (
                                    pair.second.second?.div(10f)
                                        ?: 0f
                                    ),
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

@Preview
@Composable
fun LiftCardEmptyPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        LiftCard(
            state = LiftCardState(
                lift = Lift(
                    name = "Bench Press",
                    color = 0xFF4CAF50uL
                ),
                values = emptyList(),
                maxWeight = null,
                maxReps = null
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun LiftCardWeightPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        LiftCard(
            state = LiftCardState(
                lift = Lift(
                    name = "Squat",
                    color = 0xFF2196F3uL
                ),
                values = listOf(
                    LocalDate(2024, 1, 1) to LiftCardData(weight = 135.0, reps = 5, rpe = 7),
                    LocalDate(2024, 1, 8) to LiftCardData(weight = 145.0, reps = 5, rpe = 8),
                    LocalDate(2024, 1, 15) to LiftCardData(weight = 155.0, reps = 5, rpe = 9),
                    LocalDate(2024, 1, 22) to LiftCardData(weight = 165.0, reps = 5, rpe = 8),
                    LocalDate(2024, 1, 29) to LiftCardData(weight = 175.0, reps = 5, rpe = 9)
                ),
                maxWeight = 175.0,
                maxReps = 5.0
            ),
            onClick = {},
            value = LiftCardYValue.Weight
        )
    }
}

@Preview
@Composable
fun LiftCardRepsPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        LiftCard(
            state = LiftCardState(
                lift = Lift(
                    name = "Pull Ups",
                    color = 0xFFFF9800uL
                ),
                values = listOf(
                    LocalDate(2024, 1, 1) to LiftCardData(weight = 0.0, reps = 8, rpe = 6),
                    LocalDate(2024, 1, 8) to LiftCardData(weight = 0.0, reps = 10, rpe = 7),
                    LocalDate(2024, 1, 15) to LiftCardData(weight = 0.0, reps = 12, rpe = 8),
                    LocalDate(2024, 1, 22) to LiftCardData(weight = 0.0, reps = 11, rpe = 7),
                    LocalDate(2024, 1, 29) to LiftCardData(weight = 0.0, reps = 15, rpe = 9)
                ),
                maxWeight = 0.0,
                maxReps = 15.0
            ),
            onClick = {},
            value = LiftCardYValue.Reps
        )
    }
}

@Composable
fun weightFormat(
    weight: Double,
    uom: UOM = LocalUnitOfMeasure.current,
    forceShowDecimal: Boolean = false,
    useGrouping: Boolean = false,
): String {
    return "${weight.decimalFormat(showDecimal = forceShowDecimal, grouping = useGrouping)} ${uom.value}"
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
