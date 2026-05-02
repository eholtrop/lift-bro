package com.lift.bro.presentation.dashboard

import androidx.compose.runtime.Composable
import com.lift.bro.data.LiftDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.settings.AnalyticsConsent
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.Setting
import com.lift.bro.ui.card.lift.LiftCardData
import com.lift.bro.ui.card.lift.LiftCardState
import com.lift.bro.ui.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
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
    data object AnalyticsBanner: DashboardListItem()

    @Serializable
    sealed class LiftCard: DashboardListItem() {

        @Serializable
        data class Loaded(val state: LiftCardState): LiftCard()

        @Serializable
        data object Loading: LiftCard()
    }

    @Serializable
    data object ReleaseNotes: DashboardListItem()

    @Serializable
    data object WorkoutCalendar: DashboardListItem()
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
    data object EnableAnalytics: DashboardEvent
    data object DismissAnalyticsBanner: DashboardEvent
    data class SortingOptionSelected(val sortingOption: SortingOption): DashboardEvent
    data object FavouritesAtTopToggled: DashboardEvent
}

@Composable
fun rememberDashboardInteractor(
    v3: Boolean,
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    variationRepository: IVariationRepository = dependencies.variationRepository,
    setRepository: ISetRepository = dependencies.setRepository,
    settingsRepository: ISettingsRepository = dependencies.settingsRepository,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): DashboardInteractor = rememberInteractor<DashboardState, DashboardEvent>(
    initialState = Loading,
    sideEffects = listOf(
        SideEffect { _, _, event ->
            when (event) {
                DashboardEvent.AddLiftClicked -> navCoordinator.present(Destination.EditLift(null))
                is DashboardEvent.LiftClicked -> navCoordinator.present(Destination.LiftDetails(event.liftId))
                DashboardEvent.EnableAnalytics -> {
                    settingsRepository.set(
                        Setting.AnalyticsConsent,
                        AnalyticsConsent(true, true)
                    )
                    dependencies.analytics.setConsent(true)
                }
                DashboardEvent.DismissAnalyticsBanner -> {
                    settingsRepository.set(
                        Setting.AnalyticsConsent,
                        AnalyticsConsent(true, false)
                    )
                    dependencies.analytics.setConsent(false)
                }
                else -> {}
            }
        }
    ),
    reducers = listOf(dashboardReducer),
    source = { state ->
        combine(
            liftRepository.listenAll().onStart { emit(emptyList()) },
            variationRepository.listenAll().onStart { emit(emptyList()) },
            settingsRepository.listen(Setting.AnalyticsConsent),
        ) { lifts, variations, consent -> Triple(lifts, variations, consent) }
            .flatMapLatest { (lifts, variations, consent) ->
                val variationsByLift = variations.groupBy { it.lift?.id }
                val cards: List<Flow<DashboardListItem?>> = lifts.map { lift ->
                    val liftVariations = variationsByLift[lift.id] ?: emptyList()
                    setRepository.listenAllForLift(lift.id, limit = 50)
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
                    .map { cards ->
                        cards.sortWithSettings(if (state is Loaded) state.sortingSettings else SortingSettings())
                    }
                    .map { items ->
                        Loaded(
                            items = items.toMutableList().apply {
                                if (!v3) {
                                    // lift header at top if not v3
                                    add(0, DashboardListItem.ReleaseNotes)
                                    if (!consent.dashboardBannerDismissed) {
                                        add(0, DashboardListItem.AnalyticsBanner)
                                    }
                                    add(0, DashboardListItem.LiftHeader(v3))
                                } else {
                                    // release notes -> workout calendar -> lift header
                                    add(0, DashboardListItem.LiftHeader(v3))
                                    add(0, DashboardListItem.WorkoutCalendar)
                                    add(0, DashboardListItem.ReleaseNotes)
                                    if (!consent.dashboardBannerDismissed) {
                                        add(0, DashboardListItem.AnalyticsBanner)
                                    }
                                }
                            },
                            sortingSettings = if (state is Loaded) state.sortingSettings else SortingSettings()
                        )
                    }
            }
    },
)
