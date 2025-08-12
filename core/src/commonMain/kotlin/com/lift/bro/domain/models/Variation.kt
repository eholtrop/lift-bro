package com.lift.bro.domain.models

import androidx.compose.runtime.Composable
import com.benasher44.uuid.uuid4
import com.lift.bro.presentation.LocalEMaxSettings
import com.lift.bro.presentation.LocalTMaxSettings
import com.lift.bro.presentation.lift.uom
import com.lift.bro.utils.decimalFormat
import kotlinx.serialization.Serializable

/**
 * A domain model to represent the variation class
 *
 * @property eMax: the highest estimated max of this variation s.t. the set rep > 1
 * @property eMax: the highest max of this variation s.t. the set rep == 1
 */
@Serializable
data class Variation(
    val id: String = uuid4().toString(),
    val lift: Lift? = null,
    val name: String? = null,
    val eMax: Double? = null,
    val oneRepMax: Double? = null,
    val favourite: Boolean = false,
    val notes: String? = null,
)

val Variation.fullName get() = "$name ${lift?.name}"

@Composable
fun Variation.maxText(): String {
    return when {
        // and show tmax enabled
        eMax != null && oneRepMax != null && LocalTMaxSettings.current ->
            "${
                oneRepMax.decimalFormat().uom()
            } max${if(eMax > oneRepMax) "- (${eMax.decimalFormat().uom()} tmax)" else ""}"

        eMax != null && LocalEMaxSettings.current ->
            "${eMax.decimalFormat().uom()} emax"

        oneRepMax != null -> "${
            oneRepMax.decimalFormat().uom()
        } max"

        else -> "No max"
    }
}