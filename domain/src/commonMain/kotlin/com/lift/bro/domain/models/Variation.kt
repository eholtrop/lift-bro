package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

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
    val bodyWeight: Boolean? = false,
)

val Variation.fullName get() = "${name?.trim() ?: ""} ${lift?.name?.trim()}".trim()
