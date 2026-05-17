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
        name = "Back Squat"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Front Squat"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Squat" },
        name = "Zercher Squat"
    ),
    // press variations
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Bench Press"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Floor Press"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Incline Bench Press"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Press" },
        name = "Military Press"
    ),
    // deadlift variations
    Movement(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Sumo Deadlift"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Conventional Deadlift"
    ),
    Movement(
        lift = defaultSBDLifts.first { it.name == "Deadlift" },
        name = "Negative Deadlift"
    ),
)

val defaultSbdLifts = Backup(
    lifts = defaultSBDLifts,
    variations = debugVariations,
    sets = emptyList(),
    liftingLogs = emptyList(),
)
