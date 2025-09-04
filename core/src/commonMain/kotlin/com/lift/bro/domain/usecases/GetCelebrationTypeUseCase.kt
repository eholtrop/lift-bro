package com.lift.bro.domain.usecases

import com.lift.bro.data.SetDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.CelebrationType
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.estimateMax
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlin.text.compareTo

object GetCelebrationTypeUseCase {
    operator fun invoke(
        setDataSource: SetDataSource = dependencies.database.setDataSource,
        variationDataSource: IVariationRepository = dependencies.database.variantDataSource,
    ): Flow<CelebrationType> = variationDataSource.listenAll()
        .scan(
            Pair(
                emptyList<Variation>(),
                emptyList<Variation>()

            )
        ) { old, new -> old.second to new }
        .map { (old, new) ->
            val variationMap = mutableMapOf<String, Pair<Variation?, Variation?>>()

            old.forEach {
                variationMap[it.id] = it to null
            }

            new.forEach {
                variationMap[it.id] = variationMap[it.id]?.first to it
            }

            variationMap.filter { it.value.first != it.value.second }.toList()
                .map { (_, pair) ->
                    val (old, new) = pair
                    when {
                        old?.oneRepMax != null && new?.oneRepMax != null -> {
                            if (old.oneRepMax.weight < new.oneRepMax.weight) {
                                CelebrationType.NewOneRepMax(old.oneRepMax, new.oneRepMax)
                            } else {
                                CelebrationType.None
                            }
                        }
                        old?.eMax != null && new?.eMax != null -> {
                            if ((old.eMax.estimateMax ?: 0.0) < (new.eMax.estimateMax ?: 0.0)) {
                                CelebrationType.NewEMax(old.eMax, new.eMax)
                            } else {
                                CelebrationType.None
                            }
                        }
                        else -> CelebrationType.None
                    }
                }.firstOrNull() ?: CelebrationType.None
        }

}