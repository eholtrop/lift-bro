package com.lift.bro.domain.usecases

import com.lift.bro.data.SetDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.CelebrationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

object GetCelebrationTypeUseCase {
    operator fun invoke(
        setDataSource: SetDataSource = dependencies.database.setDataSource
    ): Flow<CelebrationType> {
        val initialSets: Pair<Map<String, Double?>?, Map<String, Double?>> =
            null to setDataSource.getAll().groupBy { it.variationId }
                .mapValues { it.value.maxOfOrNull { it.weight } ?: 0.0 }

        return setDataSource.listenAll()
            .map {
                it.groupBy { it.variationId }
                    .mapValues { it.value.maxOfOrNull { it.weight } ?: 0.0 }
            }
            .scan(initialSets) { previousSets, newSets ->
                previousSets.second to newSets
            }.filter { it.first != null }
            .map {
                if (it.first != it.second) {
                    CelebrationType.Variation
                } else {
                    CelebrationType.None
                }
            }
    }
}