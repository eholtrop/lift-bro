package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String = uuid4().toString(),
    val name: String = "",
    val color: ULong? = null,
)
