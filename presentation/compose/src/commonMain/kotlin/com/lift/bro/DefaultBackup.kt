package com.lift.bro

import com.benasher44.uuid.uuid4
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Variation

private val defaultSBDLifts = listOf(
    Category(
        id = uuid4().toString(),
        name = "Squat",
        color = null,
    ),
    Category(
        id = uuid4().toString(),
        name = "Press",
        color = null
    ),
    Category(
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
    Variation(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Zercher"
    ),
    // press variations
    Variation(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Bench"
    ),
    Variation(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Floor"
    ),
    Variation(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Incline Bench"
    ),
    Variation(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Military"
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
    Variation(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Negative"
    ),
)

val defaultSbdLifts = Backup(
    lifts = defaultSBDLifts,
    variations = debugVariations,
    sets = emptyList(),
    liftingLogs = emptyList(),
)
