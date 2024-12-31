package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate

data class Day(
    val id: String = uuid4().toString(),
    val date: LocalDate,
    val notes: String,
)