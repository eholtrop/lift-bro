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

    val liftQueries get() = database.liftQueries
    val setQueries get() = database.setQueries
    val variationQueries get() = database.variationQueries
    val exerciseQueries get() = database.exerciseQueries
    val workoutQueries get() = database.workoutQueries
    val goalQueries get() = database.goalQueries
    val liftingLogQueries get() = database.liftingLogQueries
    val filterQueries get() = database.filterQueries
    val filterConditionQueries get() = database.filterConditionQueries
}

private val instantAdapter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant = Instant.fromEpochMilliseconds(databaseValue)
    override fun encode(value: Instant): Long = value.toEpochMilliseconds()
}

private val dateAdapter = object : ColumnAdapter<LocalDate, Long> {
    override fun decode(databaseValue: Long): LocalDate = LocalDate.fromEpochDays(databaseValue.toInt())
    override fun encode(value: LocalDate): Long = value.toEpochDays()
}
