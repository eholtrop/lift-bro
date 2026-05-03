package com.lift.bro.data

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.data.datasource.LBExerciseDataSource
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource
import com.lift.bro.data.sqldelight.datasource.toDomain
import com.lift.bro.db.LiftBroDB
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.calculateMax
import com.lift.bro.domain.models.estimatedMax
import com.lift.bro.domain.repositories.Sorting
import comliftbrodb.CategoryQueries
import comliftbrodb.GetAllByMovement
import comliftbrodb.Goal
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import tv.dpal.ext.flow.mapEach
import tv.dpal.ktx.datetime.toLocalDate
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

    @Deprecated("use the liftRepository instead")
    val liftDataSource: LiftDataSource = LiftDataSource(
        database.categoryQueries,
        database.setQueries,
        database.variationQueries
    )

    // Deprecated: repositories are constructed in DI using data:core + data:sqldelight implementations
    // Keeping LBDatabase focused on providing access to queries/datasources and helpers.

    @Deprecated("use the setRepository instead")
    val setDataSource: SetDataSource = SetDataSource(
        setQueries = database.setQueries,
        variationQueries = database.variationQueries
    )

    val logDataSource = database.liftingLogQueries

    @Deprecated("use the workoutRepository instead")
    val workoutDataSource = database.workoutQueries

    // Expose queries for DI wiring of SQLDelight-backed datasources
    val categoryQueries get() = database.categoryQueries
    val setQueries get() = database.setQueries
    val movementQueries get() = database.movementQueries
    val exerciseQueries get() = database.exerciseQueries

    val workoutQueries get() = database.workoutQueries

    val goalQueries get() = database.goalQueries

    suspend fun clear() {
        database.categoryQueries.deleteAll()
        database.variationQueries.deleteAll()
        database.setQueries.deleteAll()
        database.exerciseQueries.deleteAll()
        database.workoutQueries.deleteAll()
    }

    val exerciseDataSource = LBExerciseDataSource(
        exerciseQueries = database.exerciseQueries,
        setQueries = database.setQueries,
        variationQueries = database.variationQueries
    )

    val variantDataSource: VariationDataSource = SqlDelightVariationDataSource(
        categoryQueries = database.categoryQueries,
        setQueries = database.setQueries,
        movementQueries = database.movementQueries
    )
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
        return setQueries.getAllByMovement(variationId, limit).executeAsList().map { set ->
            val oneRepMax = setQueries.getOneRepMaxForMovement(variationId, before = set.date)
                .executeAsOneOrNull()
            val eMax =
                setQueries.getEMaxForMovement(variationId, before = set.date).executeAsOneOrNull()

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

    fun listenAllForCategory(
        variationId: String,
    ): Flow<List<LBSet>> =
        setQueries.getAllByMovement(variationId, Long.MAX_VALUE).flowToList(dispatcher)
            .map { sets ->
                sets.map { set ->
                    val localMax =
                        sets.filter { it.movementId == set.movementId }
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
        setQueries.getAllSets(
            limit = limit,
            reps = null,
            startDate = startDate,
            endDate = endDate,
            movementId = variationId,
            sortBy = Sorting.date.toString(),
            order = 0,
        ).executeAsList().map { it.toDomain() }

    fun LocalDate.atStartOfDayIn(): Instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())

    fun LocalDate.atEndOfDayIn(): Instant =
        this.atTime(23, 59, 59, 999999999).toInstant(TimeZone.currentSystemDefault())

    fun get(setId: String?): LBSet? = setQueries.get(setId ?: "").executeAsOneOrNull()?.toDomain()

    suspend fun save(set: LBSet) {
        withContext(dispatcher) {
            setQueries.save(
                id = set.id,
                movementId = set.variationId,
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
        setQueries.deleteAllFromVariations(movementId = variationId)
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
    variationId = this.movementId,
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

fun GetAllByMovement.toDomain() = LBSet(
    id = this.id,
    variationId = this.movementId,
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
    private val categoryQueries: CategoryQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun get(id: String?): Flow<Category?> =
        categoryQueries.get(id ?: "").asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    fun listenAll(): Flow<List<Category>> =
        categoryQueries.getAll().asFlow().mapToList(dispatcher).mapEach { it.toDomain() }

    fun getAll(): List<Category> =
        categoryQueries.getAll().executeAsList().map { it.toDomain() }

    fun save(lift: Category): Boolean {
        GlobalScope.launch(dispatcher) {
            categoryQueries.save(
                lift.id,
                lift.name,
                lift.color?.toLong(),
            )
        }
        return true
    }

    suspend fun deleteAll() {
        categoryQueries.deleteAll()
    }

    suspend fun delete(liftId: String) {
        categoryQueries.delete(liftId)
    }
}

internal fun comliftbrodb.Lift.toDomain() = Category(
    id = this.id,
    name = this.name,
    color = this.color?.toULong(),
)

// internal fun GetAll.toDomain(): Movement {
//    return Movement(
//        id = this.id,
//        lift = Category(
//            id = this.lift_id,
//            name = this.lift_name,
//            color = this.lift_color?.toULong(),
//        ),
//        name = this.name,
//        eMax = null,
//        maxReps = null,
//        oneRepMax = null,
//        bodyWeight = this.body_weight?.let { it == 1L },
//    )
// }

internal fun comliftbrodb.Variation.toDomain(
    parentLift: Category?,
    sets: List<LBSet>,
) = Movement(
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
