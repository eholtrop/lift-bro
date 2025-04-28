@file:OptIn(ExperimentalAnimationSpecApi::class, ExperimentalAnimationSpecApi::class)

package com.lift.bro.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.keyframesWithSpline
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.fullName
import com.lift.bro.ui.theme.spacing
import com.lift.bro.domain.models.UOM
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.weight_selector_chin_subtitle
import lift_bro.core.generated.resources.weight_selector_chin_title
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

expect object DecimalFormat {
    fun formatWeight(weight: Double?): String
}

@Composable
fun WeightSelector(
    modifier: Modifier = Modifier,
    variation: Variation,
    weight: Pair<Double?, UOM>,
    placeholder: String,
    weightChanged: (Pair<Double?, UOM>) -> Unit,
) {
    val sets = dependencies.database.setDataSource.getAllForLift(variation.lift?.id ?: "")
    val liftMax = sets.maxByOrNull { it.weight }
    val variationMax = sets.filter { it.variationId == variation.id }.maxByOrNull { it.weight }



    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var percentageDenominator by remember { mutableStateOf(liftMax ?: variationMax) }

        var currentValue by remember {
            mutableStateOf(DecimalFormat.formatWeight(weight.first))
        }


        DecimalPicker(
            modifier = Modifier.fillMaxWidth(),
            title = placeholder,
            selectedNum = currentValue.toDoubleOrNull(),
            numberChanged = {
                currentValue = DecimalFormat.formatWeight(it)
                weightChanged(weight.copy(first = it))
            },
            suffix = {
                val uom by dependencies.settingsRepository.getUnitOfMeasure().collectAsState(null)
                uom?.let {
                    Text(it.uom.value)
                }
            },
        )

        androidx.compose.material3.Card(
            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small.copy(
                topStart = CornerSize(0.dp),
                topEnd = CornerSize(0.dp),
            )
        ) {
            Box(
                modifier = Modifier.padding(
                    vertical = MaterialTheme.spacing.half, horizontal = MaterialTheme.spacing.one
                )
            ) {
                weight.first?.let { weight ->
                    Column {
                        if (liftMax != null) {
                            val percentage = ((weight / liftMax.weight) * 100).toInt()
                            Text(
                                text = stringResource(
                                    Res.string.weight_selector_chin_title,
                                    percentage,
                                    variation.lift?.name ?: "",
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (variationMax != null) {
                            val percentage = ((weight / variationMax.weight) * 100).toInt()
                            Text(
                                text = stringResource(
                                    Res.string.weight_selector_chin_subtitle,
                                    percentage,
                                    variation.fullName,
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

val FloatVectorConverter: TwoWayConverter<Float, AnimationVector1D> = TwoWayConverter(
    convertToVector = { AnimationVector1D(it) },
    convertFromVector = { it.value }

)

val OffsetVectorConverter: TwoWayConverter<Offset, AnimationVector2D> =
    TwoWayConverter(
        convertToVector = { AnimationVector2D(it.x, it.y) },
        convertFromVector = { Offset(it.v1, it.v2) }
    )

data class Confetti(
    val position: Animatable<Offset, AnimationVector2D>,
    val color: Color,
    val size: Dp,
    val rotation: Animatable<Float, AnimationVector1D>,
    val scale: Animatable<Float, AnimationVector1D>,
    val alpha: Animatable<Float, AnimationVector1D>,
    val direction: Int = if (Random.nextBoolean()) 1 else -1,
) {
    companion object {
        fun generate(width: Float, height: Float, random: Random): Confetti {
            val startX = (width / 2f) + random.nextInt(-50, 50)
            val startY = -50f

            return Confetti(
                position = Animatable(Offset(startX, startY), Offset.VectorConverter),
                color = Color(random.nextInt()),
                size = (random.nextInt(5, 15)).dp,
                rotation = Animatable(0f),
                scale = Animatable(0.5f + random.nextFloat() * 0.5f),
                alpha = Animatable(1f)
            )
        }
    }
}

@OptIn(ExperimentalAnimationSpecApi::class)
@Composable
fun ConfettiExplosion(
    modifier: Modifier = Modifier,
    confettiCount: Int = 100,
    durationMillis: Int = 3000,
    delayMillis: Long = 10
) {
    val density = LocalDensity.current
    val confettiList = remember { mutableStateOf<List<Confetti>>(emptyList()) }
    val random = remember { Random.Default }
    val scope = rememberCoroutineScope()
    val animationJob = remember { mutableStateOf<Job?>(null) }

    fun triggerConfetti() {
        animationJob.value?.cancel()
        animationJob.value = scope.launch {
            val height = density.density * 1000f
            val width = density.density * 400f
            val newConfetti = List(confettiCount) {
                Confetti.generate(width, height, random) // Adjust bounds as needed
            }
            confettiList.value = newConfetti

            newConfetti.forEach { confetti ->
                val targetX = confetti.position.value.x + random.nextFloat() * confetti.direction * 500f
                val targetY = height * Random.nextFloat()

                launch {
                    confetti.position.animateTo(
                        targetValue = Offset(targetX, targetY),
                        animationSpec = keyframesWithSpline {
                            this.durationMillis = durationMillis
                            Offset((width / 2f) + random.nextInt(-50, 50), 0f) at 0
                            Offset(targetX, targetY) at durationMillis
                        }
                    )
                }
                launch {
                    confetti.rotation.animateTo(
                        targetValue = random.nextFloat() * 720f - 360f,
                        animationSpec = tween(
                            durationMillis = durationMillis,
                            easing = LinearEasing
                        )
                    )
                }
                launch {
                    confetti.scale.animateTo(
                        targetValue = 0.1f + random.nextFloat() * 0.5f,
                        animationSpec = tween(
                            durationMillis = durationMillis,
                            easing = LinearEasing
                        )
                    )
                }
                launch {
                    confetti.alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = durationMillis,
                            easing = LinearEasing
                        )
                    )
                }
                delay(delayMillis)
            }
            delay(durationMillis.toLong())
            confettiList.value = emptyList() // Clear confetti after animation
        }
    }

    LaunchedEffect(Unit) {
        // You can trigger the confetti based on an event here
        // For example, after a successful action:
        delay(1000)
        triggerConfetti()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        confettiList.value.forEach { confetti ->
            val position = confetti.position.value
            val sizePx = with(density) { confetti.size.toPx() }

            rotate(degrees = confetti.rotation.value, pivot = position) {
                scale(
                    scaleX = confetti.scale.value,
                    scaleY = confetti.scale.value,
                    pivot = position
                ) {
                    drawRect(
                        color = confetti.color.copy(alpha = confetti.alpha.value),
                        size = Size(sizePx, sizePx),
                        topLeft = confetti.position.value,
                    )
                    // You can draw other shapes here like rectangles or triangles
                }
            }
        }
    }
}