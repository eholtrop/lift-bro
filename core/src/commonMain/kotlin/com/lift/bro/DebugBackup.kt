package com.lift.bro

import androidx.compose.ui.graphics.Color
import com.benasher44.uuid.uuid4
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

private val debugLifts = listOf(
    Lift(
        id = "1",
        name = "Squat",
        color = null,
    ),
    Lift(
        id = "2",
        name = "Press",
        color = Color.Red.value,
    ),
    Lift(
        id = "3",
        name = "Deadlift",
        color = Color.Blue.value,
    ),
)

private val debugVariations = listOf(
    // squat variations
    Variation(
        id = "back squat",
        lift = debugLifts.first { it.id == "1" },
        name = "Back"
    ),
    Variation(
        id = "front squat",
        lift = debugLifts.first { it.id == "1" },
        name = "Front"
    ),
    // press variations
    Variation(
        id = "military press",
        lift = debugLifts.first { it.id == "2" },
        name = "Military"
    ),
    Variation(
        id = "bench press",
        lift = debugLifts.first { it.id == "2" },
        name = "Bench"
    ),
    // deadlift variations
    Variation(
        id = "sumo deadlift",
        lift = debugLifts.first { it.id == "3" },
        name = "Sumo"
    ),
    Variation(
        id = "regular deadlift",
        lift = debugLifts.first { it.id == "3" },
        name = "Regular"
    ),
)

val debugSets = listOf(
    LBSet(
        id = uuid4().toString(),
        variationId = "bench press",
        weight = 150.0,
        reps = 1,
        notes = "Hold at 90 degrees"
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 120.0,
        reps = 1,
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "regular deadlift",
        weight = 140.0,
        reps = 1,
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 130.0,
        reps = 1,
        date = Clock.System.now().minus(24, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 170.0,
        reps = 1,
        date = Clock.System.now().minus(72, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 190.0,
        reps = 1,
        date = Clock.System.now().minus(102, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 167.0,
        reps = 1,
        date = Clock.System.now().minus(132, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 153.0,
        reps = 1,
        date = Clock.System.now().minus(162, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 142.0,
        reps = 1,
        date = Clock.System.now().minus(192, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 123.0,
        reps = 1,
        date = Clock.System.now().minus(222, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 172.0,
        reps = 1,
        date = Clock.System.now().minus(252, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "regular deadlift",
        weight = 170.0,
        reps = 1,
        date = Clock.System.now().minus(102, DateTimeUnit.HOUR),
        notes = ""
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "bench press",
        weight = 190.0,
        reps = 1,
        date = Clock.System.now().minus(200, DateTimeUnit.HOUR),
        notes = ""
    ),
)

val debugBackup = Backup(
    lifts = debugLifts,
    variations = debugVariations,
    sets = debugSets,
    liftingLogs = emptyList(),
)