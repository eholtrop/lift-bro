package com.lift.bro.presentation.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.ui.navigation.Destination.CreateSet
import com.lift.bro.ui.navigation.Destination.EditSet
import com.lift.bro.ui.navigation.Destination.VariationDetails
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.navi.LocalNavCoordinator
import tv.dpal.navi.NavCoordinator

typealias CategoryDetailsInteractor = Interactor<CategoryDetailsState, CategoryDetailsEvent>

@Serializable
data class CategoryDetailsState(
    val categoryId: String,
    val categoryName: String? = null,
    val categoryColor: ULong? = null,
    val movements: List<MovementCardState> = emptyList(),
)

@Serializable
data class MovementCardState(
    val variation: Movement,
    val sets: List<LBSet>,
)

sealed interface CategoryDetailsEvent {
    data class CategoryColorChanged(val color: ULong): CategoryDetailsEvent
    data class NameUpdated(val name: String): CategoryDetailsEvent
    data class MovementClicked(val variation: Movement): CategoryDetailsEvent
    data class SetClicked(val lbSet: LBSet): CategoryDetailsEvent
    data class ToggleFavourite(val variation: Movement): CategoryDetailsEvent
    data object CreateMovementClicked: CategoryDetailsEvent
    data object DeleteCategoryClicked: CategoryDetailsEvent
    data object AddSetClicked: CategoryDetailsEvent
}

@Composable
fun rememberCategoryDetailsInteractor(
    categoryId: String,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): CategoryDetailsInteractor {
    return rememberInteractor(
        initialState = CategoryDetailsState(
            categoryId = categoryId,
        ),
        source = { state ->
            combine(
                dependencies.liftRepository.get(categoryId),
                dependencies.variationRepository.listenAll(categoryId),
                dependencies.setRepository.listenAllForLift(categoryId)
                    .map { it.groupBy { it.variationId } }
            ) { lift, variations, sets ->
                CategoryDetailsState(
                    categoryId = state.categoryId,
                    categoryName = lift?.name,
                    categoryColor = lift?.color,
                    movements = variations.map {
                        MovementCardState(
                            variation = it,
                            sets = sets[it.id] ?: emptyList()
                        )
                    },
                )
            }
        },
        sideEffects = listOf(
            SideEffect { _, state, event ->
                when (event) {
                    CategoryDetailsEvent.AddSetClicked -> navCoordinator.present(
                        CreateSet(
                            liftId = categoryId
                        )
                    )

                    is CategoryDetailsEvent.CategoryColorChanged -> {
                        dependencies.liftRepository.save(
                            Category(
                                id = state.categoryId,
                                name = state.categoryName ?: "",
                                color = event.color,
                            )
                        )
                    }

                    is CategoryDetailsEvent.SetClicked ->
                        navCoordinator.present(
                            EditSet(
                                setId = event.lbSet.id
                            )
                        )

                    is CategoryDetailsEvent.MovementClicked ->
                        navCoordinator.present(
                            VariationDetails(
                                variationId = event.variation.id
                            )
                        )

                    is CategoryDetailsEvent.ToggleFavourite -> {
                        dependencies.database.variantDataSource.save(
                            event.variation.copy(
                                favourite = !event.variation.favourite
                            )
                        )
                    }

                    is CategoryDetailsEvent.NameUpdated -> dependencies.liftRepository.save(
                        Category(
                            id = state.categoryId,
                            name = event.name,
                            color = state.categoryColor
                        )
                    )

                    CategoryDetailsEvent.CreateMovementClicked -> navCoordinator.present(
                        VariationDetails("")
                    )

                    CategoryDetailsEvent.DeleteCategoryClicked -> {
                        dependencies.liftRepository.delete(state.categoryId)
                        navCoordinator.onBackPressed(false)
                    }
                }
            }
        )
    )
}
