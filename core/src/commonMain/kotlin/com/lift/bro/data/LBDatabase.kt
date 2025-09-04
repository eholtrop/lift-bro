package com.lift.bro.data

import androidx.compose.ui.unit.IntRect
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.lift.bro.db.LiftBroDB
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.calculateMax
import com.lift.bro.domain.models.estimatedMax
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.debug
import com.lift.bro.utils.mapEach
import com.lift.bro.utils.toLocalDate
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
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

    val variantDataSource: IVariationRepository = VariationRepository(
        liftQueries = database.liftQueries,
        variationQueries = database.variationQueries,
        setQueries = database.setQueries
    )

    val setDataSource: SetDataSource = SetDataSource(
        setQueries = database.setQueries,
        variationQueries = database.variationQueries
    )

    val logDataSource = database.liftingLogQueries

    val workoutDataSource = database.workoutQueries
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
): ISetRepository {

    private fun calculateMer(set: LiftingSet, maxWeight: Double): Int {
        if (maxWeight <= 0.0) return 0
        val repFatigueCost = 4

        val weight = set.weight ?: 0.0
        val reps = set.reps ?: 0

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
                set.toDomain().copy(mer = calculateMer(set, localMax))
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
        variationQueries
            .getAllForLift(liftId).asFlow().mapToList(dispatcher)
            .flatMapLatest { variations ->
                merge(
                    *variations.map {
                        setQueries.getAllByVariation(it.id, limit).asFlow().mapToList(dispatcher)
                    }.toTypedArray()
                ).scan(emptyList()) { acc, sets ->
                    acc + sets.map { it.toDomain() }
                }
            }


    fun listenAllForVariation(
        variationId: String,
    ): Flow<List<LBSet>> =
        setQueries.getAllByVariation(variationId, Long.MAX_VALUE).asFlow().mapToList(dispatcher)
            .map { sets ->
                sets.map { set ->
                    val localMax =
                        sets.filter { it.variationId == set.variationId }
                            .filter { it.date.toLocalDate() < set.date.toLocalDate() }
                            .maxOfOrNull { calculateMax(it.reps, it.weight) }
                    set.toDomain().copy(
                        mer = localMax?.let { calculateMer(set, localMax) } ?: 0
                    )
                }
            }

    fun getAll(): List<LBSet> =
        setQueries.getAll(
            limit = Long.MAX_VALUE,
            startDate = Instant.DISTANT_PAST,
            endDate = Instant.DISTANT_FUTURE
        ).executeAsList().map { it.toDomain() }

    fun LocalDate.atStartOfDayIn(): Instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())

    fun LocalDate.atEndOfDayIn(): Instant = this.atTime(23, 59, 59, 999).toInstant(TimeZone.currentSystemDefault())

    fun listenAll(
        startDate: LocalDate = LocalDate.fromEpochDays(0),
        endDate: LocalDate = Instant.DISTANT_FUTURE.toLocalDate(),
        variationId: String? = null,
        limit: Long = Long.MAX_VALUE,
    ): Flow<List<LBSet>> =
        setQueries.getAll(
            limit = limit,
            startDate = startDate.atStartOfDayIn(),
            endDate = endDate.atEndOfDayIn()
        ).asFlow().mapToList(dispatcher).map { sets ->
            sets
                .filter { variationId == null || it.variationId == variationId }
                .map { set ->
                    val orm = setQueries.getOneRepMaxForVariation(variationId = set.variationId, before = set.date).executeAsOneOrNull()?.weight
                    val emax = setQueries.getEMaxForVariation(variationId = set.variationId, before = set.date).executeAsOneOrNull()?.weight

                    set.toDomain().copy(
                        mer = (orm ?: emax)?.let { calculateMer(set, it) } ?: 0
                    )
                }
        }

    fun get(setId: String?): LBSet? = setQueries.get(setId ?: "").executeAsOneOrNull()?.toDomain()

    override suspend fun save(set: LBSet) {
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

    suspend fun deleteAll(variationId: String) {
        setQueries.deleteAllFromVariations(variationId = variationId)
    }

    suspend fun deleteAll() {
        setQueries.deleteAll()
    }

    suspend fun delete(setId: String) {
        setQueries.delete(setId)
    }

    override fun listen(id: String): Flow<LBSet?> =
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

internal fun comliftbrodb.Variation.toDomain(
    parentLift: Lift?,
    sets: List<LiftingSet>,
) = Variation(
    id = this.id,
    lift = parentLift,
    name = this.name,
    eMax = sets.filter { (it.reps ?: 0) > 1 }.maxByOrNull {
        estimatedMax(it.reps?.toInt() ?: 1, it.weight ?: 0.0)
    }?.toDomain(),
    maxReps = sets.maxByOrNull { it.reps ?: 0L }?.toDomain(),
    oneRepMax = sets.filter { it.reps == 1L }.maxByOrNull { it.weight ?: 0.0 }?.toDomain(),
    favourite = this.favourite == 1L,
    notes = this.notes
)

expect class DriverFactory {

    fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>,
    ): SqlDriver
}
