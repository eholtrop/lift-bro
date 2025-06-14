package com.lift.bro.data

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
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.mapEach
import comliftbrodb.LiftQueries
import comliftbrodb.LiftingLog
import comliftbrodb.LiftingSet
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class LBDatabase(
    driverFactory: DriverFactory,
) {

    private val database by lazy {
        LiftBroDB(
            driverFactory.provideDbDriver(LiftBroDB.Schema),
            LiftingSetAdapter = LiftingSet.Adapter(dateAdapter = instantAdapter),
            LiftingLogAdapter = LiftingLog.Adapter(dateAdapter = dateAdapter),
        )
    }

    val liftDataSource: LiftDataSource = LiftDataSource(
        database.liftQueries,
        database.setQueries,
        database.variationQueries
    )

    val variantDataSource: IVariationRepository = VariationRepository(
        liftQueries = database.liftQueries,
        variationQueries = database.variationQueries
    )

    val setDataSource: SetDataSource = SetDataSource(
        setQueries = database.setQueries,
        variationQueries = database.variationQueries
    )

    val logDataSource = database.liftingLogQueries
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
        return value.toEpochDays().toLong()
    }
}


class SetDataSource(
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getAll(variationId: String): List<LBSet> =
        setQueries.getAllByVariation(variationId).executeAsList()
            .map {
                it.toDomain()
            }

    fun getAllForLift(liftId: String): List<LBSet> =
        variationQueries.getAllForLift(liftId).executeAsList().map {
            setQueries.getAllByVariation(it.id).executeAsList().map { it.toDomain() }
        }
            .fold(emptyList()) { list, subList -> list + subList }

    fun listenAllForLift(liftId: String): Flow<List<LBSet>> = combine(
        variationQueries.getAllForLift(liftId).asFlow().mapToList(dispatcher),
        setQueries.getAll().asFlow().mapToList(dispatcher),
    ) { variations, sets ->
        sets.filter { set -> variations.any { it.id == set.variationId } }
    }.mapEach { it.toDomain() }

    fun listenAllForVariation(variationId: String): Flow<List<LBSet>> =
        setQueries.getAllByVariation(variationId).asFlow().mapToList(dispatcher)
            .mapEach { it.toDomain() }

    fun getAll(): List<LBSet> = setQueries.getAll().executeAsList().map { it.toDomain() }

    fun listenAll(): Flow<List<LBSet>> =
        setQueries.getAll().asFlow().mapToList(dispatcher).mapEach { it.toDomain() }

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

    private fun LiftingSet.toDomain() = LBSet(
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
    )
}

class LiftDataSource(
    private val liftQueries: LiftQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun get(id: String?): Flow<Lift?> =
        liftQueries.get(id ?: "").asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    fun listenAll(): Flow<List<Lift>> = combine(
        liftQueries.getAll().asFlow().mapToList(dispatcher),
        variationQueries.getAll().asFlow().mapToList(dispatcher),
        setQueries.getAll().asFlow().mapToList(dispatcher),
    ) { lifts, variations, sets ->
        lifts.map { lift ->
            lift.toDomain().copy(
                maxWeight = sets.filter { set ->
                    variations.filter { it.liftId == lift.id }
                        .any { it.id == set.variationId }
                }.maxOfOrNull { it.weight ?: 0.0 }
            )
        }
    }

    // TODO: make sure to populate maxWeight here
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

internal fun comliftbrodb.Variation.toDomain(parentLift: Lift) = Variation(
    id = this.id,
    lift = parentLift,
    name = this.name,
)

expect class DriverFactory {

    fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ): SqlDriver
}
