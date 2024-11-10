package com.lift.bro

import com.benasher44.uuid.uuid4
import com.lift.bro.data.Backup
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

val debugLifts = listOf(
    Lift(
        id = "1",
        name = "Squat"
    ),
    Lift(
        id = "2",
        name = "Press"
    ),
    Lift(
        id = "3",
        name = "Deadlift"
    ),
    Lift(
        id = "4",
        name = "Dead lift"
    ),
    Lift(
        id = "5",
        name = "Dead lift"
    ),
    Lift(
        id = "6",
        name = "Dead lift"
    ),
    Lift(
        id = "7",
        name = "Dead lift"
    ),
    Lift(
        id = "8",
        name = "Dead lift"
    )
)

private val debugVariations = listOf(
    // squat variations
    Variation(
        id = "back squat",
        liftId = "1",
        name = "Back"
    ),
    Variation(
        id = "front squat",
        liftId = "1",
        name = "Front"
    ),
    // press variations
    Variation(
        id = "military press",
        liftId = "2",
        name = "Military"
    ),
    Variation(
        id = "bench press",
        liftId = "2",
        name = "Bench"
    ),
    // deadlift variations
    Variation(
        id = "sumo deadlift",
        liftId = "3",
        name = "Sumo"
    ),
    Variation(
        id = "regular deadlift",
        liftId = "3",
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
        variationId = "back squat",
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
        date = Clock.System.now().minus(72, DateTimeUnit.HOUR),
        notes = ""
    ),
)

val debugBackup = Backup(
    lifts = debugLifts,
    variations = debugVariations,
    sets = debugSets,
)