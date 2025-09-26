package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import comliftbrodb.SetQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqldelightSetDataSource(
    private val setQueries: SetQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SetDataSource {

    override fun listen(id: String): Flow<LBSet?> =
        setQueries.get(id).asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    override suspend fun save(lbSet: LBSet) {
        setQueries.save(
            id = lbSet.id,
            variationId = lbSet.variationId,
            weight = lbSet.weight,
            reps = lbSet.reps,
            tempoDown = lbSet.tempo.down,
            tempoHold = lbSet.tempo.hold,
            tempoUp = lbSet.tempo.up,
            date = lbSet.date,
            notes = lbSet.notes,
            rpe = lbSet.rpe?.toLong(),
        )
    }

    override suspend fun delete(lbSet: LBSet) {
        setQueries.delete(lbSet.id)
    }
}

private fun comliftbrodb.LiftingSet.toDomain() = LBSet(
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
    notes = this.notes ?: "",
    rpe = this.rpe?.toInt(),
)
