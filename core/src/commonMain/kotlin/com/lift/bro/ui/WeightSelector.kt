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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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