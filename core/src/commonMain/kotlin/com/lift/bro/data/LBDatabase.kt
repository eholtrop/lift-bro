package com.lift.bro.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.lift.bro.db.LiftBroDB
import comliftbrodb.Lift
import comliftbrodb.LiftQueries
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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

    val setDataSource: SetQueries = database.setQueries
}

class LiftDataSource(
    val liftQueries: LiftQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun get(id: String?): Flow<Lift?> = liftQueries.get(id ?: "").asFlow().mapToOneOrNull(dispatcher)

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
