package com.lift.bro.data

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.data.datasource.LBExerciseDataSource
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource
import com.lift.bro.db.LiftBroDB
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.calculateMax
import com.lift.bro.domain.models.estimatedMax
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.utils.mapEach
import com.lift.bro.utils.toLocalDate
import comliftbrodb.GetAllByVariation
import comliftbrodb.LiftQueries
import comliftbrodb.LiftingLog
import comliftbrodb.LiftingSet
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import comliftbrodb.Workout
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.math.min

class LBDatabase(
    driverFactory: DriverFactory,
) {

    private val database by lazy {
        LiftBroDB(
            driverFactory.provideDbDriver(LiftBroDB.Schema),
            LiftingSetAdapter = LiftingSet.Adapter(dateAdapter = instantAdapter),
            LiftingLogAdapter = LiftingLog.Adapter(dateAdapter = dateAdapter),
            WorkoutAdapter = Workout.Adapter(dateAdapter = dateAdapter),
        )
    }

    val liftDataSource: LiftDataSource = LiftDataSource(
        database.liftQueries,
        database.setQueries,
        database.variationQueries
    )

    // Deprecated: repositories are constructed in DI using data:core + data:sqldelight implementations
    // Keeping LBDatabase focused on providing access to queries/datasources and helpers.

    val setDataSource: SetDataSource = SetDataSource(
        setQueries = database.setQueries,
        variationQueries = database.variationQueries
    )

    val logDataSource = database.liftingLogQueries

    val workoutDataSource = database.workoutQueries

    // Expose queries for DI wiring of SQLDelight-backed datasources
    val liftQueries get() = database.liftQueries
    val setQueries get() = database.setQueries
    val variationQueries get() = database.variationQueries
    val exerciseQueries get() = database.exerciseQueries

    val workoutQueries get() = database.workoutQueries

    val exerciseDataSource = LBExerciseDataSource(
        exerciseQueries = database.exerciseQueries,
        setQueries = database.setQueries,
        variationQueries = database.variationQueries
    )

    val variantDataSource: VariationDataSource = SqlDelightVariationDataSource(
        liftQueries = database.liftQueries,
        setQueries = database.setQueries,
        variationQueries = database.variationQueries
    )
}

private val instantAdapter = object: ColumnAdapter<Instant, Long> {

    override fun decode(databaseValue: Long): Instant {
        return Instant.fromEpochMilliseconds(databaseValue)
    }

    override fun encode(value: Instant): Long {
        return value.toEpochMilliseconds()
    }
}

private val dateAdapter = object: ColumnAdapter<LocalDate, Long> {

    override fun decode(databaseValue: Long): LocalDate {
        return LocalDate.fromEpochDays(databaseValue.toInt())
    }

    override fun encode(value: LocalDate): Long {
        return value.toEpochDays().toLong()
    }
}

class SetDataSource(
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private fun calculateMer(setWeight: Double?, setReps: Long?, maxWeight: Double): Int {
        if (maxWeight <= 0.0) return 0
        val repFatigueCost = 4

        val weight = setWeight ?: 0.0
        val reps = setReps ?: 0

        val merFatigueThreshold = 80.0

        val setFatigue = ((weight / maxWeight) * 100.0) + (reps * repFatigueCost)

        return min(reps.toInt(), ((setFatigue - merFatigueThreshold) / 4.0).toInt())
    }

    fun getAll(variationId: String, limit: Long = Long.MAX_VALUE): List<LBSet> {
        return setQueries.getAllByVariation(variationId, limit).executeAsList().map { set ->
            val oneRepMax = setQueries.getOneRepMaxForVariation(variationId, before = set.date)
                .executeAsOneOrNull()
            val eMax =
                setQueries.getEMaxForVariation(variationId, before = set.date).executeAsOneOrNull()

            val localMax =
                listOf(oneRepMax, eMax).maxOfOrNull { calculateMax(it?.reps, it?.weight) }

            localMax?.let {
                set.toDomain().copy(
                    mer = calculateMer(
                        setWeight = set.weight,
                        setReps = set.reps,
                        maxWeight = localMax
                    )
                )
            } ?: run {
                set.toDomain()
            }
        }
    }

    fun getAllForLift(liftId: String, limit: Long = Long.MAX_VALUE): List<LBSet> =
        variationQueries.getAllForLift(liftId).executeAsList().map {
            getAll(variationId = it.id, limit)
        }
            .fold(emptyList()) { list, subList -> list + subList }

    fun listenAllForLift(liftId: String, limit: Long = Long.MAX_VALUE): Flow<List<LBSet>> =
        setQueries.getAllForLift(
            liftId = liftId,
            startDate = Instant.DISTANT_PAST,
            endDate = Instant.DISTANT_FUTURE,
            limit = limit
        )
            .flowToList()
            .mapEach {
                LBSet(
                    id = it.id,
                    variationId = it.variationId,
                    weight = it.weight ?: 0.0,
                    reps = it.reps ?: 1,
                    tempo = Tempo(
                        down = it.tempoDown ?: 3,
                        hold = it.tempoHold ?: 1,
                        up = it.tempoUp ?: 1,
                    ),
                    date = it.date,
                    notes = it.notes,
                    rpe = it.rpe?.toInt(),
                    mer = 0,
                    bodyWeightRep = it.body_weight?.let { it == 1L },
                )
            }


    fun listenAllForVariation(
        variationId: String,
    ): Flow<List<LBSet>> =
        setQueries.getAllByVariation(variationId, Long.MAX_VALUE).flowToList(dispatcher)
            .map { sets ->
                sets.map { set ->
                    val localMax =
                        sets.filter { it.variationId == set.variationId }
                            .filter { it.date.toLocalDate() < set.date.toLocalDate() }
                            .maxOfOrNull { calculateMax(it.reps, it.weight) }
                    set.toDomain().copy(
                        mer = localMax?.let { calculateMer(set.weight, set.reps, localMax) } ?: 0
                    )
                }
            }

    fun getAll(
        limit: Long = Long.MAX_VALUE,
        startDate: Instant? = null,
        endDate: Instant? = null,
        variationId: String? = null,
    ): List<LBSet> =
        setQueries.getAll(
            limit = limit,
            startDate = startDate,
            endDate = endDate,
            variationId = variationId
        ).executeAsList().map { it.toDomain() }

    fun LocalDate.atStartOfDayIn(): Instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())

    fun LocalDate.atEndOfDayIn(): Instant =
        this.atTime(23, 59, 59, 999).toInstant(TimeZone.currentSystemDefault())

    fun listenAll(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        variationId: String? = null,
        limit: Long = Long.MAX_VALUE,
    ): Flow<List<LBSet>> =
        setQueries.getAll(
            limit = limit,
            startDate = startDate?.atStartOfDayIn(),
            endDate = endDate?.atEndOfDayIn(),
            variationId = variationId,
        ).flowToList()
            .map { sets ->
                sets
                    .map { set ->
                        val orm = setQueries.getOneRepMaxForVariation(
                            variationId = set.variationId,
                            before = set.date
                        ).executeAsOneOrNull()?.weight
                        val emax = setQueries.getEMaxForVariation(
                            variationId = set.variationId,
                            before = set.date
                        ).executeAsOneOrNull()?.weight

                        set.toDomain().copy(
                            mer = (orm ?: emax)?.let { calculateMer(set.weight, set.reps, it) } ?: 0
                        )
                    }
            }

    fun get(setId: String?): LBSet? = setQueries.get(setId ?: "").executeAsOneOrNull()?.toDomain()

    suspend fun save(set: LBSet) {
        withContext(dispatcher) {
            setQueries.save(
                id = set.id,
                variationId = set.variationId,
                weight = set.weight,
                reps = set.reps,
                tempoDown = set.tempo.down,
                tempoHold = set.tempo.hold,
                tempoUp = set.tempo.up,
                date = set.date,
                notes = set.notes,
                rpe = set.rpe?.toLong(),
            )
        }
    }

    suspend fun delete(lbSet: LBSet) {
        setQueries.delete(lbSet.id)
    }

    suspend fun deleteAll(variationId: String) {
        setQueries.deleteAllFromVariations(variationId = variationId)
    }

    suspend fun deleteAll() {
        setQueries.deleteAll()
    }

    suspend fun delete(setId: String) {
        setQueries.delete(setId)
    }

    fun listen(id: String): Flow<LBSet?> =
        setQueries.get(id).asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }
}

fun LiftingSet.toDomain() = LBSet(
    id = this.id,
    variationId = this.variationId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    tempo = Tempo(
        down = this.tempoDown ?: 3,
        hold = this.tempoHold ?: 1,
        up = this.tempoUp ?: 1,
    ),
    date = this.date,
    notes = this.notes,
    rpe = this.rpe?.toInt(),
    )

fun GetAllByVariation.toDomain() = LBSet(
    id = this.id,
    variationId = this.variationId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    tempo = Tempo(
        down = this.tempoDown ?: 3,
        hold = this.tempoHold ?: 1,
        up = this.tempoUp ?: 1,
    ),
    date = this.date,
    notes = this.notes,
    rpe = this.rpe?.toInt(),
    bodyWeightRep = this.body_weight?.let { it == 1L },
)

class LiftDataSource(
    private val liftQueries: LiftQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun get(id: String?): Flow<Lift?> =
        liftQueries.get(id ?: "").asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    fun listenAll(): Flow<List<Lift>> =
        liftQueries.getAll().asFlow().mapToList(dispatcher).mapEach { it.toDomain() }

    fun getAll(): List<Lift> =
        liftQueries.getAll().executeAsList().map { it.toDomain() }

    fun save(lift: Lift): Boolean {
        GlobalScope.launch(dispatcher) {
            liftQueries.save(
                lift.id,
                lift.name,
                lift.color?.toLong(),
            )
        }
        return true
    }

    suspend fun deleteAll() {
        liftQueries.deleteAll()
    }

    suspend fun delete(liftId: String) {
        liftQueries.delete(liftId)
    }
}

internal fun comliftbrodb.Lift.toDomain() = Lift(
    id = this.id,
    name = this.name,
    color = this.color?.toULong(),
)

internal fun comliftbrodb.GetAll.toDomain(): Variation {
    return Variation(
        id = this.id,
        lift = Lift(
            id = this.lift_id,
            name = this.lift_name,
            color = this.lift_color?.toULong(),
        ),
        name = this.name,
        eMax = null,
        maxReps = null,
        oneRepMax = null,
        bodyWeight = this.body_weight?.let { it == 1L },
    )
}

internal fun comliftbrodb.Variation.toDomain(
    parentLift: Lift?,
    sets: List<LBSet>,
) = Variation(
    id = this.id,
    lift = parentLift,
    name = this.name,
    eMax = sets.filter { it.reps > 1 }.maxByOrNull {
        estimatedMax(it.reps.toInt(), it.weight)
    },
    maxReps = sets.maxByOrNull { it.reps },
    oneRepMax = sets.filter { it.reps == 1L }.maxByOrNull { it.weight },
    favourite = this.favourite == 1L,
    notes = this.notes,
    bodyWeight = this.body_weight?.let { it == 1L },
)

expect class DriverFactory {

    fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
    ): SqlDriver
}
