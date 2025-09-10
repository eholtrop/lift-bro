package com.lift.bro.domain.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.benasher44.uuid.uuid4
import com.lift.bro.presentation.LocalEMaxSettings
import com.lift.bro.presentation.LocalTMaxSettings
import com.lift.bro.presentation.lift.uom
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.estimateMax
import com.lift.bro.utils.formattedReps
import com.lift.bro.utils.oneRepMax
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.roundToInt

typealias VariationId = String

/**
 * A domain model to represent the variation class
 *
 * @property eMax: the highest estimated max of this variation s.t. the set rep > 1
 * @property eMax: the highest max of this variation s.t. the set rep == 1
 */
@Serializable
data class Variation(
    val id: VariationId = uuid4().toString(),
    val lift: Lift? = null,
    val name: String? = null,
    val reps: Long = 1,
    val favourite: Boolean = false,
    val notes: String? = null,
    val eMax: LBSet? = null,
    val oneRepMax: LBSet? = null,
    val maxReps: LBSet? = null,
    val bodyWeight: Boolean? = null,
)

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
            eMax?.estimateMax != null && oneRepMax?.oneRepMax != null && LocalTMaxSettings.current ->
                append(
                    "${
                        oneRepMax.weight.decimalFormat().uom()
                    } max${
                        if (eMax.estimateMax!! > oneRepMax.oneRepMax!!) " - (${
                            eMax.estimateMax?.roundToInt()
                        } tmax)" else ""
                    }"
                )

            oneRepMax?.oneRepMax != null -> append(
                "${
                    oneRepMax.weight.decimalFormat().uom()
                } max"
            )

            eMax?.estimateMax != null -> {
                append("${eMax.reps} x ${eMax.weight.decimalFormat().uom()}")
                if (LocalEMaxSettings.current) {
                    append(" (${eMax.estimateMax?.roundToInt()} emax)")
                }
            }

        }
    }
}