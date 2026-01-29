# MVI (Model–View–Intent) for Kotlin Multiplatform

A tiny, Compose-first MVI toolkit designed to be easy to learn, portable across KMP targets, and simple to embed in existing apps. It provides just enough structure to build unidirectional data flows without locking you into a large framework.

- Reducer: pure (State, Event) -> State
- SideEffect: suspend (State, Event) -> Unit
- Interactor: orchestrates a source Flow<State> + Event stream into a StateFlow<State>

## Why this library
- Minimal surface area that fits naturally with Kotlin Flow and Jetpack/Compose Multiplatform
- Works across Kotlin Multiplatform targets (Android, iOS, Desktop, etc.)
- Does not prescribe navigation, DI, or data layers

## Quick start

Define your State and Event, create reducers and side-effects, and wire them up in your Composable using `rememberInteractor` from `mvi:compose`.

```kotlin path=null start=null
data class CounterState(val value: Int = 0)

sealed interface CounterEvent {
    data object Increment : CounterEvent
    data object Decrement : CounterEvent
}

val counterReducer = Reducer<CounterState, CounterEvent> { state, event ->
    when (event) {
        CounterEvent.Increment -> state.copy(value = state.value + 1)
        CounterEvent.Decrement -> state.copy(value = state.value - 1)
    }
}

val logSideEffect: SideEffect<CounterState, CounterEvent> = { state, event ->
    // fire-and-forget logging, analytics, etc.
}

@Composable
fun CounterScreen() {
    val interactor = rememberInteractor(
        initialState = CounterState(),
        reducers = listOf(counterReducer),
        sideEffects = listOf(logSideEffect),
        source = { initial -> flow { emit(initial) } }, // no upstream flow in this example
    )

    val state by interactor.state.collectAsState()

    // UI... dispatch events with interactor(CounterEvent.Increment)
}
```

## Concepts

- State: a snapshot of UI data. Note: if using other wrappers they may have restrictions. ex: State must be `@Serializable` to enable automatic persistence via `rememberSaveable` in `:mvi:compose`.
- Event: user or system intent (button clicks, results, timers, etc.). Use a sealed interface.
- Reducer: pure function that computes the next state. Side-effect free.
- SideEffect: suspend function to perform I/O, navigation, any async task. Dispatch follow-up events if needed.
- Interactor: coordinates the loop; exposes `state: StateFlow<State>` and a function-call `operator fun invoke(event)` to dispatch.

## Advanced usage

- Upstream source
  Provide a `Flow<State>` for your upstream data (e.g., repositories). The interactor will fold events on top of the latest upstream emission.

```kotlin
rememberInteractor(
    initialState = MyState(),
    reducers = reducers,
    sideEffects = sideEffects,
    source = { initial -> repo.stateFlow() },
)
```

- State restoring
  `rememberSaveable` is used under the hood with kotlinx.serialization. If your `State` is not serializable, provide your own saver or disable persistence by wrapping `rememberInteractor`.

- Threading
  - Reductions run on the collector’s context; side-effects are dispatched on `Dispatchers.Default`.
  - Keep reducers pure; perform I/O in side-effects and dispatch new events when done.

- Lifecycle
  The returned `Interactor` is hosted by the provided `CoroutineScope` (defaults to a Compose scope). On Android, it behaves like a view-model–scoped state holder.

## API surface (KDoc)

All public symbols include KDoc:
- Reducer<State, Event>
- typealias SideEffect<State, Event>
- class Interactor<State, Event>
- @Composable rememberInteractor(...)

Browse the source in `src/commonMain/kotlin` for details and parameter docs.

## Installation

Until this library is published, depend on it via a composite build/module include:

```kotlin path=null start=null
// settings.gradle.kts
include(":flowvi")

// build.gradle.kts of your module
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":flowvi"))
            
            // optional
            implementation(project(":flowvi:compose"))
        }
    }
}
```

When published, you'll be able to use:

```kotlin path=null start=null
// build.gradle.kts
dependencies {
    implementation("tv.dpal:flowvi:<version>")
            
    // optional
    implementation("tv.dpal:flowvi-compose:<version>")
}
```
