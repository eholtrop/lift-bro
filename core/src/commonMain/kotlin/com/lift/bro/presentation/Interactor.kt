package com.lift.bro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun interface Reducer<State, Event> {
    operator fun invoke(state: State, event: Event): State
}

typealias SideEffect<State, Event> = suspend (State, Event) -> Unit

class Interactor<State, Event>(
    initialState: State,
    coroutineScope: CoroutineScope,
    private val reducers: List<Reducer<State, Event>> = emptyList(),
    private val sideEffects: List<SideEffect<State, Event>> = emptyList(),
) {
    private val event: Channel<Event> = Channel()

    operator fun invoke(event: Event) {}

    val state = event.receiveAsFlow()
        .scan(initialState) { state, event ->
            val newState = reducers.fold(state) { s, reducer ->
                reducer(
                    s,
                    event
                )
            }
            sideEffects.forEach { sideEffect -> sideEffect(newState, event) }
            newState
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = initialState
        )
}

@Composable
inline fun <reified State, Event> rememberInteractor(
    initialState: State,
    reducers: List<Reducer<State, Event>> = emptyList(),
    sideEffects: List<SideEffect<State, Event>> = emptyList(),
    viewModelScope: CoroutineScope = rememberCoroutineScope(),
): Interactor<State, Event> {
    return rememberSaveable(
        initialState,
        saver = object : Saver<Interactor<State, Event>, String> {
            override fun SaverScope.save(value: Interactor<State, Event>): String? {
                return Json.encodeToString(value.state)
            }

            override fun restore(value: String): Interactor<State, Event>? {
                return Interactor(
                    Json.decodeFromString<State>(value),
                    viewModelScope,
                    reducers = reducers,
                    sideEffects = sideEffects,
                )
            }
        }
    ) {
        Interactor(
            initialState,
            viewModelScope,
            reducers = reducers,
            sideEffects = sideEffects,
        )
    }
}