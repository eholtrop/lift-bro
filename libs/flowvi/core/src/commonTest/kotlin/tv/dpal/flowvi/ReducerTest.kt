package tv.dpal.flowvi

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReducerTest {

    data class TestState(val value: Int = 0, val text: String = "")

    sealed interface TestEvent {
        data object Increment : TestEvent
        data class SetValue(val newValue: Int) : TestEvent
        data class SetText(val newText: String) : TestEvent
    }

    @Test
    fun `Given a reducer When invoked with state and event Then it returns new state`() = runTest {
        // Given
        val reducer = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.Increment -> state.copy(value = state.value + 1)
                else -> state
            }
        }
        val initialState = TestState(value = 5)

        // When
        val newState = reducer(initialState, TestEvent.Increment)

        // Then
        assertEquals(6, newState.value)
    }

    @Test
    fun `Given a reducer When event doesn't match Then state is unchanged`() = runTest {
        // Given
        val reducer = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.Increment -> state.copy(value = state.value + 1)
                else -> state
            }
        }
        val initialState = TestState(value = 10, text = "original")

        // When
        val newState = reducer(initialState, TestEvent.SetText("new"))

        // Then
        assertEquals(initialState, newState)
    }

    @Test
    fun `Given a reducer that modifies specific fields When invoked Then only those fields change`() = runTest {
        // Given
        val reducer = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.SetText -> state.copy(text = event.newText)
                else -> state
            }
        }
        val initialState = TestState(value = 42, text = "old")

        // When
        val newState = reducer(initialState, TestEvent.SetText("new"))

        // Then
        assertEquals(42, newState.value) // Unchanged
        assertEquals("new", newState.text) // Changed
    }

    @Test
    fun `Given a reducer with suspend logic When invoked Then it completes successfully`() = runTest {
        // Given
        val reducer = Reducer<TestState, TestEvent> { state, event ->
            // Simulate suspend operation
            kotlinx.coroutines.delay(1)
            
            when (event) {
                is TestEvent.SetValue -> state.copy(value = event.newValue)
                else -> state
            }
        }
        val initialState = TestState(value = 0)

        // When
        val newState = reducer(initialState, TestEvent.SetValue(100))

        // Then
        assertEquals(100, newState.value)
    }

    @Test
    fun `Given multiple event types When reducer handles all Then correct transformations occur`() = runTest {
        // Given
        val reducer = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.Increment -> state.copy(value = state.value + 1)
                is TestEvent.SetValue -> state.copy(value = event.newValue)
                is TestEvent.SetText -> state.copy(text = event.newText)
            }
        }

        // When & Then
        var state = TestState(value = 0, text = "")
        
        state = reducer(state, TestEvent.Increment)
        assertEquals(TestState(value = 1, text = ""), state)

        state = reducer(state, TestEvent.SetText("hello"))
        assertEquals(TestState(value = 1, text = "hello"), state)

        state = reducer(state, TestEvent.SetValue(50))
        assertEquals(TestState(value = 50, text = "hello"), state)
    }

    @Test
    fun `Given a reducer that chains transformations When invoked Then all transformations apply`() = runTest {
        // Given
        val reducer = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.Increment -> state.copy(
                    value = state.value + 1,
                    text = "${state.text}+"
                )
                else -> state
            }
        }
        val initialState = TestState(value = 0, text = "count")

        // When
        val newState = reducer(initialState, TestEvent.Increment)

        // Then
        assertEquals(1, newState.value)
        assertEquals("count+", newState.text)
    }

    @Test
    fun `Given a pure reducer When invoked multiple times with same inputs Then results are identical`() = runTest {
        // Given
        val reducer = Reducer<TestState, TestEvent> { state, event ->
            when (event) {
                is TestEvent.SetValue -> state.copy(value = event.newValue * 2)
                else -> state
            }
        }
        val initialState = TestState(value = 5)
        val event = TestEvent.SetValue(10)

        // When
        val result1 = reducer(initialState, event)
        val result2 = reducer(initialState, event)
        val result3 = reducer(initialState, event)

        // Then - Pure function should produce identical results
        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertEquals(20, result1.value)
    }
}
