package com.lift.bro.data.sqldelight.datasource

import com.lift.bro.data.core.datasource.GoalDataSource
import com.lift.bro.domain.models.Goal
import comliftbrodb.GoalQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import comliftbrodb.Goal as GoalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class SqlDelightGoalDataSource(
    val goalQueries: GoalQueries,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
): GoalDataSource {

    override fun get(id: String): Flow<Goal?> = goalQueries.get(id).asFlowOneOrNull().map { it?.toDomain() }


    override fun getAll(limit: Long): Flow<List<Goal>> = goalQueries.getAll(limit).asFlowList(dispatcher).map { it.map { it.toDomain() } }

    override suspend fun save(goal: Goal) {
        goalQueries.save(
            id = goal.id,
            name = goal.name,
            achieved = if (goal.achieved) 1L else 0,
            created_at = goal.createdAt,
            updated_at = Clock.System.now()
        )
    }

    override suspend fun delete(goal: Goal) = goalQueries.delete(goal.id)
}

fun GoalEntity.toDomain() = Goal(
    id = this.id,
    name = this.name,
    achieved = if (this.achieved == 1L) true else false,
    createdAt = this.created_at,
    updatedAt = this.updated_at,
)
