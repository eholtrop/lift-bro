package com.lift.bro.presentation.dashboard

import androidx.compose.runtime.Composable
import com.lift.bro.data.LiftDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.di.feedAuthRepository
import com.lift.bro.di.feedRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.feed.IFeedAuthenticationRepository
import com.lift.bro.domain.feed.IFeedRepository
import com.lift.bro.domain.feed.Post
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.ui.card.lift.LiftCardData
import com.lift.bro.ui.card.lift.LiftCardState
import com.lift.bro.ui.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
data class Loaded(val items: List<DashboardListItem>): DashboardState

@Serializable
data object Loading: DashboardState

@Serializable
sealed class DashboardListItem {

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
    sealed class ATProtoItem: DashboardListItem() {
        @Serializable
        data object SignIn: ATProtoItem()

        @Serializable
        data class PostCarousel(val posts: List<Post>): ATProtoItem()
    }

    @Serializable
    data object AddLiftButton: DashboardListItem()
}

sealed interface DashboardEvent {
    data object AddLiftClicked: DashboardEvent
    data class LiftClicked(val liftId: String): DashboardEvent

    data object SignInAtProto: DashboardEvent
}

@Composable
fun rememberDashboardInteractor(
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    variationRepository: IVariationRepository = dependencies.variationRepository,
    setRepository: ISetRepository = dependencies.setRepository,
    settingsRepository: ISettingsRepository = dependencies.settingsRepository,
    feedAuthenticationRepository: IFeedAuthenticationRepository = dependencies.feedAuthRepository,
    feedRepository: IFeedRepository = dependencies.feedRepository,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): DashboardInteractor = rememberInteractor<DashboardState, DashboardEvent>(
    initialState = Loading,
    sideEffects = listOf(
        SideEffect { _, _, event ->
            when (event) {
                DashboardEvent.AddLiftClicked -> navCoordinator.present(Destination.EditLift(null))
                is DashboardEvent.LiftClicked -> navCoordinator.present(Destination.LiftDetails(event.liftId))
                DashboardEvent.SignInAtProto -> feedAuthenticationRepository.startOAuth()
            }
        }
    ),
    source = {
        combine(
            liftRepository.listenAll().onStart { emit(emptyList()) },
            variationRepository.listenAll().onStart { emit(emptyList()) },
            feedAuthenticationRepository.isAuthenticated()
                .flatMapLatest {
                    if (!settingsRepository.enableATProto()) {
                        return@flatMapLatest flowOf(
                            null
                        ) as Flow<DashboardListItem.ATProtoItem?>
                    }
                    when (it) {
                        true -> {
                            feedRepository.getWorkoutPosts(feedAuthenticationRepository.getStoredCredentials()!!)
                                .map { result ->
                                    result.fold(
                                        onSuccess = { posts ->
                                            DashboardListItem.ATProtoItem.PostCarousel(posts = posts)
                                        },
                                        onFailure = {
                                            DashboardListItem.ATProtoItem.PostCarousel(posts = emptyList())
                                        }
                                    )
                                }
                        }

                        false -> flowOf(DashboardListItem.ATProtoItem.SignIn)
                    }
                },
        ) { lifts, variations, atProto -> Triple(lifts, variations, atProto) }
            .flatMapLatest { (lifts, variations, atProto) ->
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
                        cards.sortedBy { (it as? DashboardListItem.LiftCard.Loaded)?.state?.lift?.name }
                            .sortedByDescending { item ->
                                variations.any {
                                    (item as? DashboardListItem.LiftCard.Loaded)?.state?.lift?.id == it.lift?.id && it.favourite
                                }
                            }
                    }
                    .map { items ->
                        Loaded(
                            items = items.toMutableList().apply {
                                add(0, DashboardListItem.ReleaseNotes)
                                atProto?.let {
                                    add(1, it)
                                }
                                add(DashboardListItem.AddLiftButton)
                            }
                        )
                    }
            }
    },
)
