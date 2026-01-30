package com.lift.bro.ui.card.lift

import com.lift.bro.domain.models.Tempo
import kotlinx.datetime.LocalDate

data class LiftCardNodeData(
    val date: LocalDate,
    val value: Double,
    val rpe: Int?,
    val tempo: Tempo,
)
