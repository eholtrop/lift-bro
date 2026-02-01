package tv.dpal.flowvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Compose helper to create and retain an [Interactor] across recompositions.
 *
 * Persistence:
 * - Uses rememberSaveable + kotlinx.serialization to save the current State on configuration change.
 * - Your State type must be @Serializable (or provide a custom Saver) for persistence to work.
 *
 * @param initialState Initial state to start from when no saved state exists (when source has not emitted).
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

            override fun SaverScope.save(value: Interactor<State, Event>): String {
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
