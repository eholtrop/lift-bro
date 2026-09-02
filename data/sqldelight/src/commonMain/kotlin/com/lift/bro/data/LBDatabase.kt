package com.lift.bro.data

import app.cash.sqldelight.ColumnAdapter
import com.lift.bro.db.LiftBroDB
import comliftbrodb.Goal
import comliftbrodb.LiftingLog
import comliftbrodb.LiftingSet
import comliftbrodb.Workout
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

class LBDatabase(
    driverFactory: DriverFactory,
) {

    private val database by lazy {
        LiftBroDB(
            driverFactory.provideDbDriver(LiftBroDB.Schema),
            LiftingSetAdapter = LiftingSet.Adapter(dateAdapter = instantAdapter),
            LiftingLogAdapter = LiftingLog.Adapter(dateAdapter = dateAdapter),
            WorkoutAdapter = Workout.Adapter(dateAdapter = dateAdapter),
            GoalAdapter = Goal.Adapter(created_atAdapter = instantAdapter, updated_atAdapter = instantAdapter),
        )
    }

    // Expose queries for DI wiring of SQLDelight-backed datasources
    val categoryQueries get() = database.categoryQueries
    val setQueries get() = database.setQueries
    val movementQueries get() = database.movementQueries
    val exerciseQueries get() = database.exerciseQueries

    val workoutQueries get() = database.workoutQueries
    val liftingLogQueries get() = database.liftingLogQueries

    val goalQueries get() = database.goalQueries
}

private val instantAdapter = object : ColumnAdapter<Instant, Long> {

    override fun decode(databaseValue: Long): Instant {
        return Instant.fromEpochMilliseconds(databaseValue)
    }

    override fun encode(value: Instant): Long {
        return value.toEpochMilliseconds()
    }
}

private val dateAdapter = object : ColumnAdapter<LocalDate, Long> {

    override fun decode(databaseValue: Long): LocalDate {
        return LocalDate.fromEpochDays(databaseValue.toInt())
    }

    override fun encode(value: LocalDate): Long {
        return value.toEpochDays()
    }
}
