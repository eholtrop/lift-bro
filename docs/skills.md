# FlowVi MVI Best Practices

## Overview
`tv.dpal:flowvi` is a lightweight, Compose‑first MVI toolkit for Kotlin Multiplatform.  It provides a clean separation of state, events, reducers and side‑effects for unidirectional data flow.

## Core Concepts
- **State** – immutable snapshot of the UI.  Should be a data class and, if persisted via `rememberSaveable`, marked `@Serializable`.
- **Event** – user or system intent (button clicks, navigation triggers, timer ticks).
- **Reducer** – pure function `State × Event → State`.  Must never perform I/O or mutate shared objects.
- **SideEffect** – suspend function `State × Event → Unit`.  Handles navigation, analytics, network calls.  Can dispatch new events.
- **Interactor** – orchestrates the loop: exposes `StateFlow<State>` and a function‑call operator to dispatch events.

## How to Use FlowVi
1. **Add Dependencies** – include `tv.dpal:flowvi-core` and optionally `tv.dpal:flowvi-compose` in your Gradle module.
2. **Define State & Event** – create a `data class …State` and a sealed/interface `…Event` for each screen or feature.
3. **Create Reducers & Side‑Effects** – implement pure reducers and, if needed, side‑effects in separate files or inline.
4. **Instantiate Interactor** – use `rememberInteractor` inside a composable, providing `initialState`, a list of reducers, optional side‑effects and an upstream source Flow (often a repository stream).
5. **Collect State** – `val state by interactor.state.collectAsState()`.
6. **Dispatch Events** – call `interactor(MyEvent)` or pass it to UI callbacks.

## Best Practices
| Practice                                                     | Why It Matters |
|--------------------------------------------------------------|----------------|
| **Pure Reducers** – no side effects, only state mutation     | Keeps unit tests trivial and ensures deterministic state updates |
| **Single Source of Truth** – repository Flow drives the interactor | Avoids duplicated state and race conditions |
| **Composable‑Scoped Interactor** – `rememberInteractor` in composables | Lifecycle‑aware, no manual ViewModel handling |
| **SideEffect Separation** – navigation in separate side‑effect | Keeps reducers pure and makes navigation testable |
| **State Persistence** – `rememberSaveable` via FlowVi’s integration | UI state survives configuration changes without manual saving |
| **Event‑Driven Architecture** – sealed interfaces for events | Clear intent flow and type safety |
| **Unit Tests for Reducers** – test state transitions         | Guarantees business logic correctness |
| **Interactor typealias** - Interactors should be typealiased | Cleans up the callsite/references to the interactor |

## Example: Creating a New Screen
```kotlin
// 1. Define State and Event
@Serializable data class MyScreenState(val counter: Int = 0)
sealed interface MyEvent { object Increment : MyEvent; object Decrement : MyEvent }

// 2. Create Reducer
val counterReducer = Reducer<MyScreenState, MyEvent> { state, event ->
    when (event) {
        is MyEvent.Increment -> state.copy(counter = state.counter + 1)
        is MyEvent.Decrement -> state.copy(counter = state.counter - 1)
    }
}

// 3. Optional SideEffect (e.g., analytics)
val logSideEffect: SideEffect<MyScreenState, MyEvent> = { _, event ->
    // log event
}

// 4. Interactor in Composable
@Composable
fun MyScreen() {
    val interactor = rememberInteractor(
        initialState = MyScreenState(),
        reducers = listOf(counterReducer),
        sideEffects = listOf(logSideEffect),
        source = { initial -> flowOf(initial) }
    )
    val state by interactor.state.collectAsState()
    Button(onClick = { interactor(MyEvent.Increment) }) { Text("+${state.counter}") }
}
```

## Troubleshooting Common Issues
| Symptom | Fix |
|---------|-----|
| `ClassNotFoundException: tv.dpal.flowvi.Interactor` | Ensure `tv.dpal:flowvi-core` is on the module's classpath and Gradle sync has run |
| State not persisting across rotations | Mark state `@Serializable` and use FlowVi’s `rememberSaveable` integration |
| SideEffect never runs | Verify it is included in the `sideEffects` list passed to `rememberInteractor` |

## Resources
- [FlowVi GitHub](https://github.com/dpaltv/flowvi)
- [FlowVi Docs](https://dpaltv.github.io/flowvi/)
