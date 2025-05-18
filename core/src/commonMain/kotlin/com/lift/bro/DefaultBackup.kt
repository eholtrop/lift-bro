package com.lift.bro

import com.benasher44.uuid.uuid4
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation

private val defaultSBDLifts = listOf(
    Lift(
        id = uuid4().toString(),
        name = "Squat",
        color = null,
    ),
    Lift(
        id = uuid4().toString(),
        name = "Press",
        color = null
    ),
    Lift(
        id = uuid4().toString(),
        name = "Deadlift",
        color = null,
    ),
)

private val debugVariations = listOf(
    // squat variations
    Variation(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Back"
    ),
    Variation(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Front"
    ),
    // press variations
    Variation(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Bench"
    ),
    // deadlift variations
    Variation(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Sumo"
    ),
    Variation(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Regular"
    ),
)


val defaultSbdLifts = Backup(
    lifts = defaultSBDLifts,
    variations = debugVariations,
    sets = emptyList(),
)