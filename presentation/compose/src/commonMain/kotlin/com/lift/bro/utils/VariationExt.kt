package com.lift.bro.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.estimateMax
import com.lift.bro.domain.models.oneRepMax
import com.lift.bro.presentation.LocalEMaxSettings
import com.lift.bro.presentation.LocalTMaxSettings
import com.lift.bro.presentation.lift.uom
import kotlin.math.roundToInt


val Variation.fullName get() = "${name?.trim() ?: ""} ${lift?.name?.trim()}".trim()

@Composable
fun Variation.maxText(): AnnotatedString {
    return buildAnnotatedString {
        when {
            bodyWeight == true -> {
                append(maxReps?.reps?.toString() ?: "0")
                append(" reps")
            }
            // and show tmax enabled
            eMax?.estimateMax != null && oneRepMax?.estimateMax != null && LocalTMaxSettings.current ->
                append(
                    "${
                        oneRepMax?.weight.decimalFormat().uom()
                    } max${
                        if (eMax?.estimateMax!! > oneRepMax?.oneRepMax!!) " - (${
                            eMax?.estimateMax?.roundToInt()
                        } tmax)" else ""
                    }"
                )

            oneRepMax?.oneRepMax != null -> append(
                "${
                    oneRepMax?.weight.decimalFormat().uom()
                } max"
            )

            eMax?.estimateMax != null -> {
                append("${eMax?.reps} x ${eMax?.weight.decimalFormat().uom()}")
                if (LocalEMaxSettings.current) {
                    append(" (${eMax?.estimateMax?.roundToInt()} emax)")
                }
            }

        }
    }
}