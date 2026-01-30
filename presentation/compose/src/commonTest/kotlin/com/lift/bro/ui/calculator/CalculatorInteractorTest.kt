package com.lift.bro.ui.calculator

import com.lift.bro.domain.models.UOM
import kotlinx.coroutines.test.runTest
import tv.dpal.flowvi.Reducer
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatorInteractorTest {

    private val emptyState = CalculatorState(
        total = "0",
        expression = emptyList()
    )

    // MARK: - digitReducer tests

    @Test
    fun `digitReducer adds first digit to empty state`() = runTest {
        with(digitReducer(UOM.POUNDS)) {
            val event = CalculatorEvent.DigitAdded(5)

            val result = invoke(emptyState, event)

            assertEquals(1, result.expression.size)
            assertEquals(5.0, result.expression.first().weight.value)
            assertEquals(UOM.POUNDS, result.expression.first().weight.uom)
        }
    }

    @Test
    fun `Given a non zero value When a digit is added and the decimal is not applied Then the new digit is appended to the old value`() = runTest {
        with(digitReducer(UOM.POUNDS)) {
            // Given
            val initialState = CalculatorState(
                total = "0",
                expression = listOf(
                    Segment(Weight(5.0, UOM.POUNDS), decimalApplied = false)
                )
            )
            val event = CalculatorEvent.DigitAdded(7)

            // When
            val result = invoke(initialState, event)

            // Then
            assertEquals(1, result.expression.size)
            assertEquals(57.0, result.expression.first().weight.value)
        }
    }

    @Test
    fun `Given a zero value When a digit is added Then the digit has replaced the old value`() = runTest {
        with(digitReducer(UOM.POUNDS)) {
            val initialState = CalculatorState(
                total = "0",
                expression = listOf(
                    Segment(Weight(0.0, UOM.POUNDS))
                )
            )
            val event = CalculatorEvent.DigitAdded(7)

            val result = invoke(initialState, event)

            assertEquals(1, result.expression.size)
            assertEquals(7.0, result.expression.first().weight.value)
        }
    }

    @Test
    fun `Given operator is present When digit is added Then a new expression is added`() = runTest {
        with(digitReducer(UOM.POUNDS)) {
            val initialState = CalculatorState(
                total = "0",
                expression = listOf(
                    Segment(Weight(5.0, UOM.POUNDS), Operator.Add)
                )
            )
            val event = CalculatorEvent.DigitAdded(3)

            val result = invoke(initialState, event)

            assertEquals(2, result.expression.size)
            assertEquals(5.0, result.expression[0].weight.value)
            assertEquals(3.0, result.expression[1].weight.value)
        }
    }

    @Test
    fun `Given a decimal has been applied to a non zero value when a digit is added then the value is added *after* the decimal`() = runTest {
        with(digitReducer(UOM.POUNDS)) {
            val initialState = CalculatorState(
                total = "0",
                expression = listOf(
                    Segment(Weight(5.0, UOM.POUNDS), decimalApplied = true)
                )
            )
            val event = CalculatorEvent.DigitAdded(5)

            val result = invoke(initialState, event)

            assertEquals(1, result.expression.size)
            assertEquals(5.5, result.expression.first().weight.value)
        }
    }

    @Test
    fun `Given empty state When Clear is applied Then state is unchanged`() = runTest {
        with(digitReducer(UOM.POUNDS)) {
            val event = CalculatorEvent.ActionApplied(Action.Clear)

            val result = invoke(emptyState, event)

            assertEquals(emptyState, result)
        }
    }

    // MARK: - OperatorReducer tests

    @Test
    fun `Given no operator is present When a new operator is selected Then the operator is added`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.OperatorSelected(Operator.Add)

        val result = OperatorReducer(initialState, event)

        assertEquals(1, result.expression.size)
        assertEquals(Operator.Add, result.expression.first().operation)
    }

    @Test
    fun `Given there are two segments When a new operator is selected Then the last segment is updated`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS), Operator.Subtract),
                Segment(Weight(12.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.OperatorSelected(Operator.Add)

        val result = OperatorReducer(initialState, event)

        assertEquals(2, result.expression.size)
        assertEquals(Operator.Subtract, result.expression.first().operation)
        assertEquals(Operator.Add, result.expression[1].operation)
    }

    @Test
    fun `Given an operator is present When a new operator is selected Then the existing operator is replaced`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS), Operator.Add)
            )
        )
        val event = CalculatorEvent.OperatorSelected(Operator.Multiply)

        val result = OperatorReducer(initialState, event)

        assertEquals(1, result.expression.size)
        assertEquals(Operator.Multiply, result.expression.first().operation)
    }

    @Test
    fun `Given an operator is present in a list of two segments When a new operator is selected Then the last operator is replaced`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS), Operator.Add),
                Segment(Weight(10.0, UOM.POUNDS), Operator.Add)
            )
        )
        val event = CalculatorEvent.OperatorSelected(Operator.Multiply)

        val result = OperatorReducer(initialState, event)

        assertEquals(2, result.expression.size)
        assertEquals(Operator.Add, result.expression.first().operation)
        assertEquals(Operator.Multiply, result.expression[1].operation)
    }

    @Test
    fun `OperatorReducer ignores non-operator events`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.DigitAdded(5)

        val result = OperatorReducer(initialState, event)

        assertEquals(initialState, result)
    }

    // MARK: - ToggleUOMReducer tests

    @Test
    fun `ToggleUOMReducer toggles pounds to kg`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(100.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.ToggleUOMForIndex(0)

        val result = ToggleUOMReducer(initialState, event)

        assertEquals(UOM.KG, result.expression.first().weight.uom)
    }

    @Test
    fun `ToggleUOMReducer toggles kg to pounds`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(45.0, UOM.KG))
            )
        )
        val event = CalculatorEvent.ToggleUOMForIndex(0)

        val result = ToggleUOMReducer(initialState, event)

        assertEquals(UOM.POUNDS, result.expression.first().weight.uom)
    }

    @Test
    fun `ToggleUOMReducer toggles middle segment in expression`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS), Operator.Add),
                Segment(Weight(20.0, UOM.POUNDS), Operator.Multiply),
                Segment(Weight(30.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.ToggleUOMForIndex(1)

        val result = ToggleUOMReducer(initialState, event)

        assertEquals(3, result.expression.size)
        assertEquals(UOM.POUNDS, result.expression[0].weight.uom)
        assertEquals(UOM.KG, result.expression[1].weight.uom)
        assertEquals(UOM.POUNDS, result.expression[2].weight.uom)
    }

    @Test
    fun `ToggleUOMReducer ignores non-toggle events`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(100.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.DigitAdded(5)

        val result = ToggleUOMReducer(initialState, event)

        assertEquals(initialState, result)
    }

    // MARK: - ActionReducer tests

    @Test
    fun `ActionReducer handles Clear action`() = runTest {
        val initialState = CalculatorState(
            total = "100",
            expression = listOf(
                Segment(Weight(50.0, UOM.POUNDS), Operator.Add),
                Segment(Weight(50.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.ActionApplied(Action.Clear)

        val result = ActionReducer(initialState, event)

        assertEquals(emptyList(), result.expression)
    }

    @Test
    fun `ActionReducer handles Backspace on digit`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(123.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.ActionApplied(Action.Backspace)

        val result = ActionReducer(initialState, event)

        assertEquals(1, result.expression.size)
        assertEquals(12.0, result.expression.first().weight.value)
    }

    @Test
    fun `ActionReducer handles Backspace on operator`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS), Operator.Add)
            )
        )
        val event = CalculatorEvent.ActionApplied(Action.Backspace)

        val result = ActionReducer(initialState, event)

        assertEquals(1, result.expression.size)
        assertEquals(null, result.expression.first().operation)
        assertEquals(10.0, result.expression.first().weight.value)
    }

    @Test
    fun `ActionReducer handles Backspace on single digit removes segment`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(5.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.ActionApplied(Action.Backspace)

        val result = ActionReducer(initialState, event)

        assertEquals(emptyList(), result.expression)
    }

    @Test
    fun `ActionReducer handles Backspace on empty state`() = runTest {
        val event = CalculatorEvent.ActionApplied(Action.Backspace)

        val result = ActionReducer(emptyState, event)

        assertEquals(emptyList(), result.expression)
    }

    @Test
    fun `ActionReducer handles Equals action`() = runTest {
        val initialState = CalculatorState(
            total = "100.5",
            expression = listOf(
                Segment(Weight(50.0, UOM.POUNDS), Operator.Add),
                Segment(Weight(50.5, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.ActionApplied(Action.Equals)

        val result = ActionReducer(initialState, event)

        assertEquals(1, result.expression.size)
        assertEquals(100.5, result.expression.first().weight.value)
        assertEquals(null, result.expression.first().operation)
    }

    @Test
    fun `ActionReducer handles Decimal on existing value`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.ActionApplied(Action.Decimal)

        val result = ActionReducer(initialState, event)

        assertEquals(1, result.expression.size)
        assertEquals(true, result.expression.first().decimalApplied)
    }

    @Test
    fun `ActionReducer handles Decimal on empty state`() = runTest {
        val event = CalculatorEvent.ActionApplied(Action.Decimal)

        val result = ActionReducer(emptyState, event)

        assertEquals(1, result.expression.size)
        assertEquals(0.0, result.expression.first().weight.value)
        assertEquals(true, result.expression.first().decimalApplied)
    }

    @Test
    fun `ActionReducer handles Decimal after operator`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS), Operator.Add)
            )
        )
        val event = CalculatorEvent.ActionApplied(Action.Decimal)

        val result = ActionReducer(initialState, event)

        assertEquals(2, result.expression.size)
        assertEquals(0.0, result.expression.last().weight.value)
        assertEquals(true, result.expression.last().decimalApplied)
    }

    @Test
    fun `ActionReducer ignores non-action events`() = runTest {
        val initialState = CalculatorState(
            total = "0",
            expression = listOf(
                Segment(Weight(10.0, UOM.POUNDS))
            )
        )
        val event = CalculatorEvent.DigitAdded(5)

        val result = ActionReducer(initialState, event)

        assertEquals(initialState, result)
    }

    // MARK: - Integration tests

    @Test
    fun `calculator can build complex expression`() = runTest {
        val reducers = calculatorReducers(UOM.POUNDS)
        var state = emptyState

        // Build: 10 + 20
        state = applyReducers(state, CalculatorEvent.DigitAdded(1), reducers)
        state = applyReducers(state, CalculatorEvent.DigitAdded(0), reducers)
        state = applyReducers(state, CalculatorEvent.OperatorSelected(Operator.Add), reducers)
        state = applyReducers(state, CalculatorEvent.DigitAdded(2), reducers)
        state = applyReducers(state, CalculatorEvent.DigitAdded(0), reducers)

        assertEquals(2, state.expression.size)
        assertEquals(10.0, state.expression[0].weight.value)
        assertEquals(Operator.Add, state.expression[0].operation)
        assertEquals(20.0, state.expression[1].weight.value)
    }

    @Test
    fun `calculator can clear and restart`() = runTest {
        val reducers = calculatorReducers(UOM.POUNDS)
        var state = emptyState

        // Build and clear
        state = applyReducers(state, CalculatorEvent.DigitAdded(5), reducers)
        state = applyReducers(state, CalculatorEvent.ActionApplied(Action.Clear), reducers)
        state = applyReducers(state, CalculatorEvent.DigitAdded(3), reducers)

        assertEquals(1, state.expression.size)
        assertEquals(3.0, state.expression.first().weight.value)
    }

    @Test
    fun `calculator handles backspace through operator`() = runTest {
        val reducers = calculatorReducers(UOM.POUNDS)
        var state = emptyState

        // Build: 5 +, then backspace twice
        state = applyReducers(state, CalculatorEvent.DigitAdded(5), reducers)
        state = applyReducers(state, CalculatorEvent.OperatorSelected(Operator.Add), reducers)
        state = applyReducers(state, CalculatorEvent.ActionApplied(Action.Backspace), reducers)
        state = applyReducers(state, CalculatorEvent.ActionApplied(Action.Backspace), reducers)

        assertEquals(emptyList(), state.expression)
    }

    // Helper function to apply all reducers in sequence
    private suspend fun applyReducers(
        state: CalculatorState,
        event: CalculatorEvent,
        reducers: List<Reducer<CalculatorState, CalculatorEvent>>,
    ): CalculatorState {
        return reducers.fold(state) { currentState, reducer ->
            reducer(currentState, event)
        }
    }
}
