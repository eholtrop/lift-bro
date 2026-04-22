package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Category
import comliftbrodb.CategoryQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SqldelightLiftDataSource(
    private val categoryQueries: CategoryQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LiftDataSource {

    override fun listenAll(): Flow<List<Category>> =
        categoryQueries.getAll().asFlow().mapToList(dispatcher).map { list -> list.map { it.toDomain() } }

    override fun getAll(): List<Category> = categoryQueries.getAll().executeAsList().map { it.toDomain() }

    override fun get(id: String?): Flow<Category?> =
        categoryQueries.get(id ?: "").asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    override fun save(lift: Category): Boolean {
        GlobalScope.launch(dispatcher) {
            categoryQueries.save(
                id = lift.id,
                name = lift.name,
                color = lift.color?.toLong(),
            )
        }
        return true
    }

    override suspend fun deleteAll() {
        categoryQueries.deleteAll()
    }

    override suspend fun delete(id: String) {
        categoryQueries.delete(id)
    }
}

fun comliftbrodb.Category.toDomain(): Category = Category(
    id = this.id,
    name = this.name,
    color = this.color?.toULong(),
)
