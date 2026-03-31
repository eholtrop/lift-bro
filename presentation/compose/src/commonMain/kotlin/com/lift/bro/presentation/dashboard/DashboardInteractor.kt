package com.lift.bro.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.util.fastAny
import com.lift.bro.di.dependencies
import com.lift.bro.di.filterRepository
import com.lift.bro.di.liftRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.filter.Condition
import com.lift.bro.domain.filter.DefaultFilters
import com.lift.bro.domain.filter.Field
import com.lift.bro.domain.filter.Filter
import com.lift.bro.domain.filter.FilterRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.ui.card.lift.LiftCardData
import com.lift.bro.ui.card.lift.LiftCardState
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.utils.fullName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.ktx.datetime.toLocalDate
import tv.dpal.navi.LocalNavCoordinator
import tv.dpal.navi.NavCoordinator

typealias DashboardInteractor = Interactor<DashboardState, DashboardEvent>

@Serializable
sealed interface DashboardState

@Serializable
data class Loaded(
    val items: List<DashboardListItem>,
    val sortingSettings: SortingSettings,
): DashboardState

@Serializable
data object Loading: DashboardState

@Serializable
sealed class DashboardListItem {

    @Serializable
    data class LiftHeader(val v3: Boolean): DashboardListItem()

    @Serializable
    sealed class LiftCard: DashboardListItem() {

        @Serializable
        data class Loaded(val state: LiftCardState): LiftCard()

        @Serializable
        data object Loading: LiftCard()
    }

    @Serializable
    data class NoGraphsMigration(
        val newStyle: List<GraphCard> = emptyList(),
        val oldLifts: List<GraphCard> = emptyList(),
    ): DashboardListItem()

    @Serializable
    data class GraphCard(
        val filterId: String,
        val name: String,
        val data: List<GraphCardData>,
        val weight: Boolean,
    ): DashboardListItem() {

        val max = if (weight) {
            data.maxByOrNull { it.value.weight }
        } else {
            data.maxByOrNull { it.value.reps }
        }

        val min = if (weight) {
            data.minByOrNull { it.value.weight }
        } else {
            data.minByOrNull { it.value.reps }
        }
    }

    @Serializable
    data class GraphCardData(
        val title: String,
        val value: LBSet,
    )

    @Serializable
    data object ReleaseNotes: DashboardListItem()

    @Serializable
    data object WorkoutCalendar: DashboardListItem()

    @Serializable
    data object AddLiftButton: DashboardListItem()
}

@Serializable
enum class SortingOption {
    Heaviest,
    Reps,
    Latest,
    Name
}

@Serializable
data class SortingSettings(
    val option: SortingOption = SortingOption.Name,
    val favouritesAtTop: Boolean = true,
)

sealed interface DashboardEvent {
    data object AddLiftClicked: DashboardEvent
    data class LiftClicked(val liftId: String): DashboardEvent

    data class SortingOptionSelected(val sortingOption: SortingOption): DashboardEvent
    data object FavouritesAtTopToggled: DashboardEvent
}

private val v2Source = combine(
    dependencies.liftRepository.listenAll().onStart { emit(emptyList()) },
    dependencies.variationRepository.listenAll().onStart { emit(emptyList()) }
) { lifts, variations -> lifts to variations }
    .flatMapLatest { (lifts, variations) ->
        val variationsByLift = variations.groupBy { it.lift?.id }
        val cards: List<Flow<DashboardListItem?>> = lifts.map { lift ->
            val liftVariations = variationsByLift[lift.id] ?: emptyList()
            dependencies.setRepository.listenAllForLift(lift.id, limit = 50)
                .map { sets ->
                    DashboardListItem.LiftCard.Loaded(
                        LiftCardState(
                            lift = lift,
                            values = sets.groupBy { it.date.toLocalDate() }.map {
                                it.key to LiftCardData(
                                    it.value.maxOf { it.weight },
                                    it.value.maxOf { it.reps }.toInt(),
                                    it.value.maxOfOrNull { it.rpe ?: 0 },
                                )
                            }.sortedByDescending { it.first }.take(5).reversed(),
                            maxWeight = liftVariations.maxOfOrNull {
                                it.oneRepMax?.weight ?: 0.0
                            },
                            favourite = liftVariations.any { it.favourite },
                            maxReps = liftVariations.maxOfOrNull {
                                it.maxReps?.reps?.toDouble() ?: 0.0
                            },
                        )
                    )
                }
        }

        combine(
            *cards.map { it.onStart { emit(DashboardListItem.LiftCard.Loading) } }
                .toTypedArray()
        ) { it.toList().filterNotNull() }
            .debounce { 100L }
    }


private val v3Source: Flow<List<DashboardListItem>> = combine(
    dependencies.filterRepository.listenAll(),
    dependencies.variationRepository.listenAll(),
    dependencies.setRepository.listenAll(),
) { filters, variations, sets ->
    if (filters.isEmpty())
        emptyList()
    else {
        filters.map { filter ->
            val variationIds = filter.conditions.filter<Condition.Equals<String>>("variationId").map { it.value }
            val minWeight = filter.conditions.filter<Condition.Min<Double>>("weight").minOfOrNull { it.value } ?: 0.0
            val maxWeight = filter.conditions.filter<Condition.Min<Double>>("weight").minOfOrNull { it.value } ?: Double.MAX_VALUE

            DashboardListItem.GraphCard(
                filterId = filter.id,
                name = filter.name,
                data = sets.filter { set ->
                    variationIds.contains(set.variationId) &&
                        set.weight > minWeight && set.weight < maxWeight
                }.map {
                    DashboardListItem.GraphCardData(
                        title = it.date.toString("MM d"),
                        value = it
                    )
                },
                weight = false,
            )
        }
    }
}

inline fun <reified R: Condition> List<Condition>.filter(field: String) = this
    .filterIsInstance<R>().filter {
        when (it) {
            is Condition.Equals<*> -> it.field.name == field
            is Condition.Max<*> -> it.field.name == field
            is Condition.Min<*> -> it.field.name == field
        }
    }


@Composable
fun rememberDashboardInteractor(
    v3: Boolean,
    liftRepository: ILiftRepository = dependencies.liftRepository,
    variationRepository: IVariationRepository = dependencies.variationRepository,
    setRepository: ISetRepository = dependencies.setRepository,
    filterRepository: FilterRepository = dependencies.filterRepository,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): DashboardInteractor = rememberInteractor<DashboardState, DashboardEvent>(
    initialState = Loading,
    sideEffects = listOf(
        SideEffect { _, _, event ->
            when (event) {
                DashboardEvent.AddLiftClicked -> navCoordinator.present(Destination.EditLift(null))
                is DashboardEvent.LiftClicked -> navCoordinator.present(Destination.LiftDetails(event.liftId))
                else -> {}
            }
        }
    ),
    reducers = listOf(dashboardReducer),
    source = { state ->
        if (v3) {
            v3Source
        } else {
            v2Source
        }
            .map { cards ->
                cards.sortWithSettings(if (state is Loaded) state.sortingSettings else SortingSettings())
            }
            .map { items ->
                Loaded(
                    items = items.toMutableList().apply {
                        if (v3) {
                            if (isEmpty()) {
                                val variations = dependencies.variationRepository.getAll()

                                val hasFavourite = variations.fastAny { it.favourite }
                                val hasBodyweight = variations.fastAny { it.bodyWeight == true }

                                val sbd = listOfNotNull(
                                    variations.firstOrNull { it.fullName == "Back Squat" },
                                    variations.firstOrNull { it.fullName == "Bench Press" },
                                    variations.firstOrNull { it.fullName == "Conventional Deadlift" },
                                )

                                val filters = listOfNotNull(
                                    if (hasFavourite) DefaultFilters.Favourites else null,
                                    if (hasBodyweight) DefaultFilters.Favourites else null,
                                    if (sbd.size == 3) Filter(name = "SBD", conditions = sbd.map { Condition.Equals(Field.Variation, it.id) }) else null
                                )



                                add(
                                    0, DashboardListItem.NoGraphsMigration(
                                    )
                                )
                            } else {
                                add(0, DashboardListItem.LiftHeader(v3))
                            }
                            // release notes -> workout calendar -> lift header
                            add(0, DashboardListItem.WorkoutCalendar)
                            add(0, DashboardListItem.ReleaseNotes)
                        } else {
                            // lift header at top if not v2
                            add(0, DashboardListItem.ReleaseNotes)
                            add(0, DashboardListItem.LiftHeader(v3))
                            add(DashboardListItem.AddLiftButton)
                        }
                    },
                    sortingSettings = if (state is Loaded) state.sortingSettings else SortingSettings()
                )
            }
    },
)
