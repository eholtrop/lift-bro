package com.lift.bro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun interface Reducer<State, Event> {
    suspend operator fun invoke(state: State, event: Event): State
}

typealias SideEffect<State, Event> = suspend (State, Event) -> Unit

class Interactor<State, Event>(
    initialState: State,
    source: Flow<State>,
    coroutineScope: CoroutineScope,
    private val reducers: List<Reducer<State, Event>>,
    private val sideEffects: List<SideEffect<State, Event>>,
    stateResolver: (initial: State, source: State) -> State = { _, s -> s },
) {
    private val events: Channel<Event> = Channel()

    operator fun invoke(event: Event) = events.trySend(event)

    val state: StateFlow<State> = source
        .flowOn(Dispatchers.IO)
        .flatMapLatest { sourceState ->
            events.receiveAsFlow()
                .scan(stateResolver(initialState, sourceState)) { state, event ->
                    val newState = reducers.fold(state) { s, reducer ->
                        reducer(
                            s,
                            event
                        )
                    }
                    sideEffects.forEach { sideEffect -> sideEffect(newState, event) }
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

@Composable
inline fun <reified State, Event> rememberInteractor(
    initialState: State,
    source: Flow<State> = flow { emit(initialState) },
    reducers: List<Reducer<State, Event>> = emptyList(),
    sideEffects: List<SideEffect<State, Event>> = emptyList(),
    viewModelScope: CoroutineScope = rememberCoroutineScope(),
    noinline stateResolver: (initial: State, source: State) -> State = { _, s -> s },
): Interactor<State, Event> {
    return rememberSaveable(
        initialState,
        saver = object: Saver<Interactor<State, Event>, String> {
            override fun SaverScope.save(value: Interactor<State, Event>): String? {
                val json = Json.encodeToString(value.state.value)
                Log.d("Interactor Saver", "Saving state: $json")
                return json
            }

            override fun restore(value: String): Interactor<State, Event>? {
                Log.d("Interactor Saver", "Restoring state: $value")
                return Interactor(
                    initialState = Json.decodeFromString<State>(value),
                    coroutineScope = viewModelScope,
                    stateResolver = stateResolver,
                    source = source,
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
            source = source,
            reducers = reducers,
            sideEffects = sideEffects,
        )
    }
}