package com.lift.bro.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.lift.bro.db.LiftBroDB
import com.lift.bro.presentation.variation.UOM
import comliftbrodb.Lift
import comliftbrodb.LiftQueries
import comliftbrodb.LiftingSet
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LBDatabase(
    driverFactory: DriverFactory,
) {

    private val database by lazy {
        LiftBroDB(
            driverFactory.provideDbDriver(LiftBroDB.Schema)
        )
    }

    val liftDataSource: LiftDataSource = LiftDataSource(database.liftQueries)

    val variantDataSource: VariationQueries = database.variationQueries

    val setDataSource: SetDataSource = SetDataSource(database.setQueries)
}

data class Set(
    val id: String,
    val variationId: String,
    val weight: Double = 0.0,
    val reps: Long = 1,
    val tempoDown: Long = 3,
    val tempoHold: Long = 1,
    val tempoUp: Long = 1,
)

class SetDataSource(
    private val setQueries: SetQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getAll(variationId: String): List<Set> =
        setQueries.getAllByVariation(variationId).executeAsList()
            .map {
                it.toSet()
            }

    fun get(setId: String): Set? = setQueries.get(setId).executeAsOneOrNull()?.toSet()

    suspend fun save(set: Set) {
        setQueries.save(
            id = set.id,
            variationId = set.variationId,
            weight = set.weight,
            reps = set.reps,
            tempoDown = set.tempoDown,
            tempoHold = set.tempoHold,
            tempoUp = set.tempoUp
        )
    }

    suspend fun deleteAll(variationId: String) {
        setQueries.delete(variationId = variationId)
    }

    internal fun LiftingSet.toSet() = Set(
        id = this.id,
        variationId = this.variationId,
        weight = this.weight ?: 0.0,
        reps = this.reps ?: 1,
        tempoDown = this.tempoDown ?: 3,
        tempoHold = this.tempoHold ?: 1,
        tempoUp = this.tempoUp ?: 1
    )
}

class LiftDataSource(
    val liftQueries: LiftQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun get(id: String?): Flow<Lift?> =
        liftQueries.get(id ?: "").asFlow().mapToOneOrNull(dispatcher)

    fun getAll(): Flow<List<Lift>> = liftQueries.getAll().asFlow().mapToList(dispatcher)

    fun save(lift: Lift): Boolean {
        GlobalScope.launch(dispatcher) {
            liftQueries.save(
                lift.id,
                lift.name
            )
        }
        return true
    }

}

expect class DriverFactory {

    fun provideDbDriver(
        schema: SqlSchema<QueryResult.AsyncValue<Unit>>
    ): SqlDriver
}
