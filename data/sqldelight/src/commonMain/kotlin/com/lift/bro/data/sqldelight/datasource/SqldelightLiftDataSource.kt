package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Lift
import comliftbrodb.LiftQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SqldelightLiftDataSource(
    private val liftQueries: LiftQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LiftDataSource {

    override fun listenAll(): Flow<List<Lift>> =
        liftQueries.getAll().asFlow().mapToList(dispatcher).map { list -> list.map { it.toDomain() } }

    override fun getAll(): List<Lift> = liftQueries.getAll().executeAsList().map { it.toDomain() }

    override fun get(id: String?): Flow<Lift?> =
        liftQueries.get(id ?: "").asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    override fun save(lift: Lift): Boolean {
        GlobalScope.launch(dispatcher) {
            liftQueries.save(
                id = lift.id,
                name = lift.name,
                color = lift.color?.toLong(),
            )
        }
        return true
    }

    override suspend fun deleteAll() {
        liftQueries.deleteAll()
    }

    override suspend fun delete(id: String) {
        liftQueries.delete(id)
    }
}

private fun comliftbrodb.Lift.toDomain(): Lift = Lift(
    id = this.id,
    name = this.name,
    color = this.color?.toULong(),
)
