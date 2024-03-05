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
    )
)

private val debugVariations = listOf(
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
)

val debugSets = listOf(
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 170.0,
        reps = 1,
        tempoDown = 3,
        tempoHold = 1,
        tempoUp = 1,
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "bench press",
        weight = 150.0,
        reps = 1,
        tempoDown = 3,
        tempoHold = 1,
        tempoUp = 1,
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 190.0,
        reps = 1,
        tempoDown = 3,
        tempoHold = 1,
        tempoUp = 1,
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 190.0,
        reps = 1,
        tempoDown = 3,
        tempoHold = 1,
        tempoUp = 1,
        date = Clock.System.now().minus(24, DateTimeUnit.HOUR)
    ),
    LBSet(
        id = uuid4().toString(),
        variationId = "back squat",
        weight = 190.0,
        reps = 1,
        tempoDown = 3,
        tempoHold = 1,
        tempoUp = 1,
        date = Clock.System.now().minus(72, DateTimeUnit.HOUR)
    ),
)

val debugBackup = Backup(
    lifts = debugLifts,
    variations = debugVariations,
    sets = debugSets,
)