package tv.dpal.flowvi

/**
 * Lightweight, Compose-first MVI toolkit for Kotlin Multiplatform.
 *
 * This file defines the small set of primitives used to build unidirectional data flows:
 * - Interactor: a coordinator that merges a source Flow<State> with dispatched events,
 *   applying reducers to produce a StateFlow<State> and running side effects after each reduction.
 * - Reducer: a pure function that transforms (State, Event) -> State
 * - SideEffect: a suspend function that reacts to an Event and State (e.g., I/O, navigation)
 *
 * The goal is to provide an ergonomically small surface area that works across KMP targets while
 * staying framework-agnostic and easy to embed.
 */

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Coordinates an MVI loop by combining a source [Flow] of State with user-provided events.
 *
 * Pipeline:
 * 1) A long-lived [source] flow provides upstream state (e.g., repository streams).
 * 2) Dispatched events are folded using [reducers] to produce the next State.
 * 3) After each reduction, [sideEffects] run (on Default dispatcher).
 * 4) The resulting stream is exposed as a hot [state] via [stateIn] bound to [coroutineScope].
 *
 * @param initialState The initial state value if the source has not emitted yet (and for restore).
 * @param source The upstream State flow (e.g., from repositories). It can be a simple flow { emit(initialState) }.
 * @param coroutineScope Scope used to host the resulting [StateFlow]; typically a lifecycle scope.
 * @param reducers Ordered list of pure reducers applied to every event.
 * @param sideEffects Ordered list of side-effects executed after state changes.
 * @param stateResolver Given the previously saved initial state (e.g., from restore) and the latest
 *                      emitted state from the source, return the merged state to start scanning from.
 */
class Interactor<State, Event>(
    initialState: State,
    source: Flow<State>,
    val coroutineScope: CoroutineScope,
    private val reducers: List<Reducer<State, Event>>,
    private val sideEffects: List<SideEffect<State, Event>>,
    stateResolver: (initial: State, source: State) -> State = { _, s -> s },
) {
    private val events: Channel<Event> = Channel()

    private val dispatcher: Dispatcher<Event> = Dispatcher { event ->
        GlobalScope.launch {
            events.send(event)
        }
    }

    /**
     * Dispatch an event into the MVI loop.
     *
     * This is non-blocking; the event is buffered and then processed by reducers.
     */
    operator fun invoke(event: Event) = dispatcher(event)

    /**
     * Hot StateFlow that emits the current state. Collected values are suitable for driving UI.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<State> = source
        .flatMapLatest { sourceState ->
            events.receiveAsFlow()
                .scan(stateResolver(state.value ?: initialState, sourceState)) { state, event ->
                    val newState = reducers.fold(state) { s, reducer ->
                        reducer(s, event)
                    }
                    withContext(Dispatchers.Default) {
                        sideEffects.forEach { sideEffect -> sideEffect(dispatcher, newState, event) }
                    }
                    newState
                }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = initialState
        )
}

/**
 * A pure function that takes the current state and an event, and returns a new state.
 *
 * Guidelines:
 * - Should be side-effect free; do not perform I/O here. Use [SideEffect] for that.
 * - Prefer immutability; return a copy of state with changed fields.
 * - Compose multiple reducers via the Interactor; they are applied in order.
 */
fun interface Reducer<State, Event> {
    suspend operator fun invoke(state: State, event: Event): State
}

/**
 * A suspend function that is invoked after reducers compute a new state for an incoming event.
 *
 * Use cases:
 * - Fire-and-forget operations (analytics, logging)
 * - Kicking off asynchronous work (network / database), then dispatching a follow-up event
 * - Navigation requests
 *
 * Contract:
 * - Side effects should not mutate the state directly; communicate via new events.
 * - They run on a background dispatcher (Default) to stay off the main thread.
 */
fun interface SideEffect<State, Event> {
    suspend operator fun invoke(dispatcher: Dispatcher<Event>, state: State, event: Event)
}

/**
 * A function that when invoked, will dispatched an event into the MVI loop.
 */
fun interface Dispatcher<Event> {
    operator fun invoke(event: Event)
}
