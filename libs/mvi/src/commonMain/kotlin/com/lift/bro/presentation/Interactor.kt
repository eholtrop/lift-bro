package com.lift.bro.presentation

/**
 * Lightweight, Compose-first MVI toolkit for Kotlin Multiplatform.
 *
 * This file defines the small set of primitives used to build unidirectional data flows:
 * - Reducer: a pure function that transforms (State, Event) -> State
 * - SideEffect: a suspend function that reacts to an Event and State (e.g., I/O, navigation)
 * - Interactor: a coordinator that merges a source Flow<State> with dispatched events,
 *   applying reducers to produce a StateFlow<State> and running side effects after each reduction.
 * - rememberInteractor: a Compose helper that wires an Interactor to a Composable lifecycle and
 *   persists its State via rememberSaveable using kotlinx.serialization.
 *
 * The goal is to provide an ergonomically small surface area that works across KMP targets while
 * staying framework-agnostic and easy to embed.
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
typealias SideEffect<State, Event> = suspend Dispatcher<Event>.(State, Event) -> Unit

/**
 * A function that when invoked, will dispatched an event into the MVI loop.
 */
typealias Dispatcher<Event> = (Event) -> Unit

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
@OptIn(ExperimentalCoroutinesApi::class)
class Interactor<State, Event>(
    initialState: State,
    source: Flow<State>,
    val coroutineScope: CoroutineScope,
    private val reducers: List<Reducer<State, Event>>,
    private val sideEffects: List<SideEffect<State, Event>>,
    stateResolver: (initial: State, source: State) -> State = { _, s -> s },
) {
    private val events: Channel<Event> = Channel()

    private val dispatcher: Dispatcher<Event> = { event ->
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
 * Compose helper to create and retain an [Interactor] across recompositions.
 *
 * Persistence:
 * - Uses rememberSaveable + kotlinx.serialization to save the current State on configuration change.
 * - Your State type must be @Serializable (or provide a custom Saver) for persistence to work.
 *
 * @param initialState Initial state to start from when no saved state exists.
 * @param reducers Ordered reducers applied to each event.
 * @param sideEffects Ordered side-effects run after reductions.
 * @param viewModelScope Scope that will host the resulting StateFlow; defaults to a composable scope.
 * @param stateResolver Strategy to merge previously saved state with freshly emitted source state.
 * @param source Function that generates the upstream source Flow based on the initial (or restored) state.
 */
@Composable
inline fun <reified State, Event> rememberInteractor(
    initialState: State,
    reducers: List<Reducer<State, Event>> = emptyList(),
    sideEffects: List<SideEffect<State, Event>> = emptyList(),
    viewModelScope: CoroutineScope = rememberCoroutineScope(),
    noinline stateResolver: (initial: State, source: State) -> State = { _, s -> s },
    crossinline source: (State) -> Flow<State> = { flow { emit(initialState) } },
): Interactor<State, Event> {
    return rememberSaveable(
        initialState,
        saver = object: Saver<Interactor<State, Event>, String> {

            override fun SaverScope.save(value: Interactor<State, Event>): String? {
                return Json.encodeToString(value.state.value)
            }

            override fun restore(value: String): Interactor<State, Event>? {
                val obj = Json.decodeFromString<State>(value)
                return Interactor(
                    initialState = obj,
                    coroutineScope = viewModelScope,
                    stateResolver = stateResolver,
                    source = source(obj),
                    reducers = reducers,
                    sideEffects = sideEffects,
                )
            }
        }
    ) {
        Interactor(
            initialState = initialState,
            coroutineScope = viewModelScope,
            stateResolver = stateResolver,
            source = source(initialState),
            reducers = reducers,
            sideEffects = sideEffects,
        )
    }
}
