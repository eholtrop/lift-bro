package com.lift.bro.data.sqldelight.datasource

import com.lift.bro.data.core.datasource.FilterDataSource
import com.lift.bro.domain.filter.Condition
import com.lift.bro.domain.filter.Filter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SqldelightFilterDataSource(
    private val filterQueries: comliftbrodb.FilterQueries,
    private val conditionQueries: comliftbrodb.FilterConditionQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): FilterDataSource {

    private val json = Json { encodeDefaults = true }

    override fun getAll(): Flow<List<Filter>> {
        return filterQueries.getAll().asFlowList(dispatcher).map { rows ->
            rows.map { row -> toFilter(row) }
        }
    }

    override fun get(filterId: String): Flow<Filter?> {
        return filterQueries.getById(id = filterId).asFlowOneOrNull(dispatcher).map { row ->
            row?.let { toFilter(it) }
        }
    }

    override suspend fun save(filter: Filter) {
        withContext(dispatcher) {
            filterQueries.transaction {
                filterQueries.saveFilter(id = filter.id, name = filter.name)
                // delete existing conditions
                conditionQueries.deleteConditionsForFilter(filterId = filter.id)
                // insert new conditions
                filter.conditions.forEachIndexed { index, cond ->
                    val id = com.benasher44.uuid.uuid4().toString()
                    conditionQueries.saveCondition(
                        id = id,
                        filterId = filter.id,
                        fieldType = cond::class.qualifiedName ?: "",
                        operator = cond::class.simpleName ?: "",
                        value = json.encodeToString(cond),
                    )
                }
            }
        }
    }

    override suspend fun delete(filter: Filter) {
        withContext(dispatcher) {
            filterQueries.transaction {
                conditionQueries.deleteConditionsForFilter(filterId = filter.id)
                filterQueries.deleteFilter(id = filter.id)
            }
        }
    }

    override suspend fun deleteAll() {
        withContext(dispatcher) {
            conditionQueries.deleteConditionsForFilter(filterId = "") // delete all conditions
            filterQueries.deleteAll()
        }
    }

    private fun toFilter(row: comliftbrodb.Filter): Filter {
        val condRows = conditionQueries.getAllConditionsForFilter(filterId = row.id).executeAsList()
        val conditions = condRows.map { c -> json.decodeFromString<Condition>(c.value_ ?: "") }
        return Filter(id = row.id, name = row.name ?: "", conditions = conditions)
    }
}
