package com.lift.bro

import com.benasher44.uuid.uuid4
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Movement

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
    Movement(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Back"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Front"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Zercher"
    ),
    // press variations
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Bench"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Floor"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Incline Bench"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Military"
    ),
    // deadlift variations
    Movement(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Sumo"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Regular"
    ),
    Movement(
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
