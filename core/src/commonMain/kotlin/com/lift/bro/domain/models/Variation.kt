package com.lift.bro.domain.models

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Variation(
    val id: String = uuid4().toString(),
    val liftId: String? = null,
    val name: String? = null,
)