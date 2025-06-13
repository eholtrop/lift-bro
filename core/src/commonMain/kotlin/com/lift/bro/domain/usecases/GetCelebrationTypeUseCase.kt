package com.lift.bro.domain.usecases

import com.lift.bro.data.SetDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.CelebrationType
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.IVariationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

private data class CelebrationData(
    val variationMaxes: Map<String, Double>,
    val liftMaxes: Map<String?, Double>,
    val type: CelebrationType,
)

object GetCelebrationTypeUseCase {
    operator fun invoke(
        setDataSource: SetDataSource = dependencies.database.setDataSource,
        variationDataSource: IVariationRepository = dependencies.database.variantDataSource,
    ): Flow<CelebrationType> {

        return combine(
            setDataSource.listenAll(),
            variationDataSource.listenAll(),
        ) { sets, variations ->
            sets to variations
        }.scan<Pair<List<LBSet>, List<Variation>>, CelebrationData?>(null) { previousData, newData ->
            val newVariationMaxes = newData.first.groupBy { it.variationId }
                .mapValues { entry -> entry.value.maxOf { it.weight } }

            val newLiftMaxes = newData.second.groupBy { it.lift?.id }
                .mapValues { entry -> entry.value.maxOf { newVariationMaxes[it.id] ?: 0.0 } }

            // In no changes no celebration
            if (previousData == null || (previousData.variationMaxes == newVariationMaxes && previousData.liftMaxes == newLiftMaxes)) {
                CelebrationData(
                    variationMaxes = newVariationMaxes,
                    liftMaxes = newLiftMaxes,
                    type = CelebrationType.None
                )
            } else {
                val variationDiff =
                    newVariationMaxes.filter { new -> previousData.variationMaxes.any { old -> old != new } }
                val liftDiff =
                    newLiftMaxes.filter { new -> previousData.liftMaxes.any { old -> old != new } }

                val oldLiftMaxes = previousData.liftMaxes
                val oldVariationMaxes = previousData.variationMaxes

                val liftCelebration = liftDiff.toList().fold<Pair<String?, Double>, CelebrationType?>(null) { acc, pair ->
                    if (!oldLiftMaxes.containsKey(pair.first)) {
                        CelebrationType.None
                    } else if (pair.second > oldLiftMaxes[pair.first] ?: 0.0) {
                        CelebrationType.NewLiftMax("")
                    } else {
                        acc
                    }
                }

                val variationCelebration = variationDiff.toList().fold<Pair<String?, Double>, CelebrationType?>(null) { acc, pair ->
                    if (!oldVariationMaxes.containsKey(pair.first)) {
                        CelebrationType.None
                    } else if (pair.second > oldVariationMaxes[pair.first] ?: 0.0) {
                        CelebrationType.NewVariationMax("")
                    } else {
                        acc
                    }
                }

                CelebrationData(
                    variationMaxes = newVariationMaxes,
                    liftMaxes = newLiftMaxes,
                    type = liftCelebration ?: variationCelebration ?: CelebrationType.None
                )
            }
        }.filterNotNull().map { it.type }
    }
}