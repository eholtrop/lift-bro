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
)

val Variation.fullName get() = "${name ?: ""}  ${lift?.name}".trim()

@Composable
fun Variation.maxText(): AnnotatedString {

    return buildAnnotatedString {
        when {
            // and show tmax enabled
            eMax?.estimateMax != null && oneRepMax?.oneRepMax != null && LocalTMaxSettings.current ->
                append(
                    "${
                        oneRepMax.weight.decimalFormat().uom()
                    } max${
                        if (eMax.estimateMax!! > oneRepMax.oneRepMax!!)" - (${
                            eMax.estimateMax.decimalFormat().uom()
                        } tmax)" else ""
                    }"
                )

            oneRepMax?.oneRepMax != null -> append(
                "${
                    oneRepMax.weight.decimalFormat().uom()
                } max"
            )

            eMax?.estimateMax != null && LocalEMaxSettings.current -> {
                append("${eMax.estimateMax.decimalFormat().uom()} emax")
                append(" (${eMax.weight.decimalFormat()} x ${eMax.reps})")
            }

            else -> append("No max")
        }
    }
}