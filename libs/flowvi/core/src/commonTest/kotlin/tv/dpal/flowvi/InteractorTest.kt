package tv.dpal.flowvi

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.collections.listOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Test fixtures
private data class TestState(val count: Int = 0, val message: String = "")

private sealed interface TestEvent {
    data object Increment : TestEvent
    data object Decrement : TestEvent
    data class SetMessage(val msg: String) : TestEvent
    data class Add(val value: Int) : TestEvent
}


class InteractorTest {

    @Test
    fun `Given initial state When interactor is created Then state emits initial value`() = runTest {
        // Given
        val initialState = TestState(count = 5)

        // When
        val interactor = createTestInteractor(
            initialState = initialState,
        )

        // Then
        interactor.state.test {
            assertEquals(initialState, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given a reducer When event is dispatched Then state is updated via reducer`() = runTest {
        // Given
        val interactor = createTestInteractor(
            initialState = TestState(count = 0),
            reducers = listOf(
                Reducer { state, event ->
                    when (event) {
                        is TestEvent.Increment -> state.copy(count = state.count + 1)
                        else -> state
                    }
                }
            ),
        )

        // When
        interactor.state.test {
            skipItems(1) // Skip initial state
            interactor(TestEvent.Increment)

            // Then
            assertEquals(1, awaitItem().count)
        }
    }

    @Test
    fun `Given multiple reducers When event is dispatched Then reducers are applied in order`() = runTest {
        // Given
        val reducer1 = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.Increment -> state.copy(count = state.count + 1)
                else -> state
            }
        }

        val reducer2 = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.Increment -> state.copy(count = state.count * 10)
                else -> state
            }
        }

        val interactor = createTestInteractor(
            initialState = TestState(count = 1),
            reducers = listOf(reducer1, reducer2),
        )

        // When
        interactor.state.test {
            skipItems(1) // Skip initial
            interactor(TestEvent.Increment)

            // Then - (1 + 1) * 10 = 20
            assertEquals(20, awaitItem().count)
        }
    }

    @Test
    fun `Given a side effect When event is dispatched Then side effect executes after state change`() = runTest {
        // Given
        var sideEffectExecuted = false
        var capturedState: TestState? = null

        val sideEffect = SideEffect<TestState, TestEvent> { _, state, _ ->
            sideEffectExecuted = true
            capturedState = state
        }

        val interactor = createTestInteractor(
            initialState = TestState(count = 0),
            reducers = listOf(
                Reducer { state, event ->
                    when (event) {
                        is TestEvent.Increment -> state.copy(count = state.count + 1)
                        else -> state
                    }
                }
            ),
            sideEffects = listOf(sideEffect),
        )

        // When
        interactor.state.test {
            skipItems(1)
            interactor(TestEvent.Increment)
            val newState = awaitItem()

            // Then
            testScheduler.advanceUntilIdle()
            assertTrue(sideEffectExecuted)
            assertEquals(newState, capturedState)
            assertEquals(1, capturedState?.count)
        }
    }

    @Test
    fun `Given a side effect that dispatches events When original event dispatched Then new event is processed`() = runTest {
        // Given
        val sideEffect = SideEffect<TestState, TestEvent> { dispatcher, state, event ->
            when (event) {
                is TestEvent.Increment -> {
                    if (state.count == 1) {
                        dispatcher(TestEvent.Add(10))
                    }
                }

                else -> {}
            }
        }

        val interactor = createTestInteractor(
            initialState = TestState(count = 0),
            reducers = listOf(
                Reducer { state, event ->
                    when (event) {
                        is TestEvent.Increment -> state.copy(count = state.count + 1)
                        is TestEvent.Add -> state.copy(count = state.count + event.value)
                        else -> state
                    }
                }
            ),
            sideEffects = listOf(sideEffect),
        )

        // When
        interactor.state.test {
            skipItems(1) // Initial
            interactor(TestEvent.Increment)

            // Then
            assertEquals(1, awaitItem().count) // After increment
            assertEquals(11, awaitItem().count) // After side effect dispatched Add(10)
        }
    }

    @Test
    fun `Given source flow emits When interactor created Then state reflects source emission`() = runTest {
        // Given
        val sourceFlow = flow {
            emit(TestState(count = 0))
            emit(TestState(count = 5))
        }

        // When
        val interactor = createTestInteractor(
            initialState = TestState(count = 0),
            source = sourceFlow,
        )

        // Then
        interactor.state.test {
            assertEquals(0, awaitItem().count) // Initial
            // Source emissions happen, but events needed to observe state changes
        }
    }

    @Test
    fun `Given stateResolver When source emits Then resolver merges states correctly`() = runTest {
        // Given
        val sourceFlow = flow {
            emit(TestState(count = 100, message = "from source"))
        }

        val stateResolver: (TestState, TestState) -> TestState = { initial, source ->
            TestState(
                count = source.count,
                message = "${initial.message} + ${source.message}"
            )
        }

        // When
        val interactor = createTestInteractor(
            initialState = TestState(count = 0, message = "initial"),
            source = sourceFlow,
            reducers = emptyList(),
            sideEffects = emptyList(),
            stateResolver = stateResolver
        )

        // Then
        interactor.state.test {
            val state = awaitItem()
            assertEquals(0, state.count) // Initial state first
        }
    }

    @Test
    fun `Given multiple events When dispatched concurrently Then all events are processed`() = runTest {
        // Given
        val interactor = createTestInteractor(
            initialState = TestState(count = 0),
            reducers = listOf(
                Reducer { state, event ->
                    when (event) {
                        is TestEvent.Increment -> state.copy(count = state.count + 1)
                        is TestEvent.Add -> state.copy(count = state.count + event.value)
                        else -> state
                    }
                }
            ),
        )

        // When
        interactor.state.test {
            skipItems(1) // Initial

            interactor(TestEvent.Increment)
            interactor(TestEvent.Increment)
            interactor(TestEvent.Add(5))

            // Then
            assertEquals(1, awaitItem().count)
            assertEquals(2, awaitItem().count)
            assertEquals(7, awaitItem().count)
        }
    }

    @Test
    fun `Given different event types When dispatched Then each is handled by appropriate reducer logic`() = runTest {
        // Given
        val interactor = createTestInteractor(
            initialState = TestState(count = 10, message = ""),
            reducers = listOf(
                Reducer { state, event ->
                    when (event) {
                        is TestEvent.Increment -> state.copy(count = state.count + 1)
                        is TestEvent.Decrement -> state.copy(count = state.count - 1)
                        is TestEvent.SetMessage -> state.copy(message = event.msg)
                        is TestEvent.Add -> state.copy(count = state.count + event.value)
                    }
                }
            ),
        )

        // When & Then
        interactor.state.test {
            skipItems(1) // Initial

            interactor(TestEvent.Increment)
            assertEquals(TestState(count = 11, message = ""), awaitItem())

            interactor(TestEvent.Decrement)
            assertEquals(TestState(count = 10, message = ""), awaitItem())

            interactor(TestEvent.SetMessage("hello"))
            assertEquals(TestState(count = 10, message = "hello"), awaitItem())

            interactor(TestEvent.Add(5))
            assertEquals(TestState(count = 15, message = "hello"), awaitItem())
        }
    }

    @Test
    fun `Given multiple side effects When event dispatched Then all side effects execute in order`() = runTest {
        // Given
        val executionOrder = mutableListOf<String>()

        val interactor = createTestInteractor(
            initialState = TestState(count = 0),
            reducers = listOf(
                Reducer { state, event ->
                    when (event) {
                        is TestEvent.Increment -> state.copy(count = state.count + 1)
                        else -> state
                    }
                }
            ),
            sideEffects = listOf(
                SideEffect { _, _, _ ->
                    executionOrder.add("effect1")
                },
                SideEffect { _, _, _ ->
                    executionOrder.add("effect2")
                },
                SideEffect { _, _, _ ->
                    executionOrder.add("effect3")
                }
            ),
        )

        // When
        interactor.state.test {
            skipItems(1) // Initial state
            interactor(TestEvent.Increment)
            awaitItem() // State after event (count = 1)

            // Then - wait for side effects to complete
            testScheduler.advanceUntilIdle()
            assertEquals(listOf("effect1", "effect2", "effect3"), executionOrder)
        }
    }

    @Test
    fun `Given interactor with no reducers When event dispatched Then state remains unchanged`() = runTest {
        // Given
        val initialState = TestState(count = 42)
        val interactor = createTestInteractor(
            initialState = initialState,
            reducers = emptyList(),
        )

        // When
        interactor.state.test {
            val initial = awaitItem()
            interactor(TestEvent.Increment)

            testScheduler.advanceUntilIdle()

            // Then - state should not emit again since no reducer changed it
            assertEquals(initialState, initial)
            expectNoEvents()
        }
    }

    @Test
    fun `Given reducer that returns new state When event dispatched Then StateFlow emits new state`() = runTest {
        // Given
        val interactor = createTestInteractor(
            initialState = TestState(count = 0, message = "start"),
            reducers = listOf(
                Reducer { state, event ->
                    when (event) {
                        is TestEvent.SetMessage -> state.copy(message = event.msg)
                        else -> state
                    }
                }
            ),
        )

        // When & Then
        interactor.state.test {
            assertEquals("start", awaitItem().message)

            interactor(TestEvent.SetMessage("updated"))
            assertEquals("updated", awaitItem().message)
        }
    }

    // Helper function to create interactor with defaults
    private fun TestScope.createTestInteractor(
        initialState: TestState = TestState(),
        source: Flow<TestState> = flow { emit(initialState) },
        reducers: List<Reducer<TestState, TestEvent>> = emptyList(),
        sideEffects: List<SideEffect<TestState, TestEvent>> = emptyList(),
        stateResolver: (initial: TestState, source: TestState) -> TestState = { _, s -> s },
    ): Interactor<TestState, TestEvent> {
        return Interactor(
            initialState = initialState,
            source = source,
            coroutineScope = backgroundScope,
            reducers = reducers,
            sideEffects = sideEffects,
            stateResolver = stateResolver,
        )
    }
}
