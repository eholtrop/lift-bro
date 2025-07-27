package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
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
)

val Variation.fullName get() = "$name ${lift?.name}"