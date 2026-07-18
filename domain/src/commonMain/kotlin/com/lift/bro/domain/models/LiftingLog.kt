package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class LiftingLog(
    val id: String = uuid4().toString(),
    val date: LocalDate,
    val notes: String,
    val vibe: Int?,
)
