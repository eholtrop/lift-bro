package com.lift.bro.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class LiftingLog(
    val id: String,
    val date: LocalDate,
    val notes: String,
    val vibe: Int?,
)
