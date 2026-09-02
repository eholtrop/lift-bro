package com.lift.bro.data

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource
import com.lift.bro.data.sqldelight.datasource.toDomain
import com.lift.bro.db.LiftBroDB
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.calculateMax
import com.lift.bro.domain.repositories.Sorting
import comliftbrodb.CategoryQueries
import comliftbrodb.GetAllByMovement
import comliftbrodb.Goal
import comliftbrodb.LiftingLog
import comliftbrodb.LiftingSet
import comliftbrodb.MovementQueries
import comliftbrodb.SetQueries
import comliftbrodb.Workout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import tv.dpal.ext.flow.mapEach
import kotlin.math.min
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
