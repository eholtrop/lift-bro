package com.lift.bro.ui.calculator

import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.models.convert
import com.lift.bro.utils.decimalFormat
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Reducer

@Serializable
data class CalculatorState(
    val total: String,
    val expression: List<Segment>,
)

@Serializable
data class Segment(
    val weight: Weight,
    val operation: Operator? = null,
    val decimalApplied: Boolean = false,
)

@Serializable
data class Weight(
    val value: Double,
    val uom: UOM,
)

sealed class CalculatorEvent {
    data class DigitAdded(val digit: Int) : CalculatorEvent()
    data class OperatorSelected(val operation: Operator) : CalculatorEvent()
    data class ActionApplied(val action: Action) : CalculatorEvent()
    data class ToggleUOMForIndex(val index: Int) : CalculatorEvent()
}

fun digitReducer(defaultUOM: UOM): Reducer<CalculatorState, CalculatorEvent> =
    Reducer { state, event ->
        if (event !is CalculatorEvent.DigitAdded) return@Reducer state
        when (state.expression.lastOrNull()?.operation == null) {
            true -> {
                val lastSegment = state.expression.lastOrNull() ?: Segment(
                    Weight(0.0, defaultUOM),
                    null
                )
                val newWeight = when (lastSegment.decimalApplied) {
                    false -> lastSegment.weight.value * 10 + event.digit
                    true -> (lastSegment.weight.value.decimalFormat(true) + event.digit).toDouble()
                }

                val newSegment = lastSegment.copy(
                    weight = lastSegment.weight.copy(newWeight)
                )
                state.copy(
                    expression = state.expression.dropLast(1) + newSegment
                )
            }

            false -> {
                state.copy(
                    expression = state.expression + Segment(
                        Weight(
                            event.digit.toDouble(),
                            UOM.POUNDS
                        )
                    )
                )
            }
        }
    }

val OperatorReducer: Reducer<CalculatorState, CalculatorEvent> = Reducer { state, event ->
    if (event !is CalculatorEvent.OperatorSelected) return@Reducer state
    val lastSegment = state.expression.last()
    val newSegment = lastSegment.copy(
        operation = event.operation
    )
    state.copy(
        expression = state.expression.dropLast(1) + newSegment
    )
}

val ToggleUOMReducer: Reducer<CalculatorState, CalculatorEvent> = Reducer { state, event ->
    if (event !is CalculatorEvent.ToggleUOMForIndex) return@Reducer state
    val segment = state.expression[event.index]
    val prefix = if (event.index == 0) {
        emptyList()
    } else {
        state.expression.subList(
            0,
            event.index
        )
    }
    val suffix =
        if (state.expression.lastIndex == event.index) {
            emptyList()
        } else {
            state.expression.subList(
                event.index + 1,
                state.expression.lastIndex + 1
            )
        }

    state.copy(
        expression = prefix + segment.copy(
            weight = segment.weight.let {
                it.copy(
                    uom = it.uom.toggle()
                )
            }
        ) + suffix
    )
}

val ActionReducer: Reducer<CalculatorState, CalculatorEvent> = Reducer { state, event ->
    if (event !is CalculatorEvent.ActionApplied) return@Reducer state
    when (event.action) {
        Action.Backspace -> {
            val lastSegment = state.expression.lastOrNull()
            val newSegment = when {
                lastSegment == null -> null
                // if operation is present nullify it
                lastSegment.operation != null -> {
                    lastSegment.copy(operation = null)
                }

                else -> {
                    val newWeight = lastSegment.weight.value
                        .decimalFormat(lastSegment.decimalApplied)
                        .dropLast(1)
                        .toDoubleOrNull()
                    when (newWeight) {
                        lastSegment.weight.value -> lastSegment.copy(decimalApplied = false)
                        null -> null
                        else -> lastSegment.copy(
                            weight = lastSegment.weight.copy(
                                newWeight
                            )
                        )
                    }
                }
            }
            state.copy(
                expression = state.expression.dropLast(1) + if (newSegment != null) {
                    listOf(
                        newSegment
                    )
                } else {
                    emptyList()
                }
            )
        }

        Action.Clear -> state.copy(expression = emptyList())
        Action.Equals -> state.copy(
            expression = listOf(
                Segment(
                    Weight(
                        state.total.toDoubleOrNull() ?: 0.0,
                        UOM.POUNDS
                    ),
                    null
                )
            )
        )

        Action.Decimal -> {
            val lastSegment = state.expression.lastOrNull()

            when {
                lastSegment != null && lastSegment.operation == null -> state.copy(
                    expression = state.expression.dropLast(1) + lastSegment.copy(
                        decimalApplied = true
                    )
                )

                else -> state.copy(
                    expression = state.expression + Segment(
                        weight = Weight(
                            0.0,
                            UOM.POUNDS
                        ),
                        decimalApplied = true
                    )
                )
            }
        }
    }
}

fun totalReducer(defaultUOM: UOM): Reducer<CalculatorState, CalculatorEvent> =
    Reducer { state, event ->
        try {
            state.copy(
                total = calculateTotal(state.expression, defaultUOM).decimalFormat()
            )
        } catch (arithmeticException: ArithmeticException) {
            state.copy(
                total = "..."
            )
        }
    }

fun calculatorReducers(defaultUOM: UOM): List<Reducer<CalculatorState, CalculatorEvent>> = listOf(
    digitReducer(defaultUOM),
    OperatorReducer,
    ActionReducer,
    ToggleUOMReducer,
    totalReducer(defaultUOM),
)

private fun UOM.toggle(): UOM {
    return when (this) {
        UOM.KG -> UOM.POUNDS
        UOM.POUNDS -> UOM.KG
    }
}

private fun calculateTotal(expression: List<Segment>, defaultUOM: UOM): Double {
    if (expression.isEmpty()) return 0.0
    val segment = expression.first()
    val nextSegment = expression.getOrNull(1)

    if (segment.operation == Operator.Divide && nextSegment?.weight?.value == 0.0) {
        throw ArithmeticException(
            "Cannot divide by zero"
        )
    }

    val thisWeight = segment.weight.uom.convert(
        segment.weight.value,
        defaultUOM
    )
    val nextWeight = segment.weight.uom.convert(
        nextSegment?.weight?.value ?: 1.0,
        defaultUOM
    )

    return when (segment.operation) {
        null -> thisWeight
        Operator.Multiply -> calculateTotal(
            listOf(
                Segment(
                    Weight(thisWeight * (nextSegment?.weight?.value ?: 1.0), defaultUOM),
                    nextSegment?.operation
                )
            ) + expression.drop(2),
            defaultUOM = defaultUOM
        )

        Operator.Divide -> calculateTotal(
            listOf(
                Segment(
                    Weight(thisWeight / if (nextWeight == 0.0) 1.0 else nextWeight, defaultUOM),
                    nextSegment?.operation
                )
            ) + expression.drop(2),
            defaultUOM = defaultUOM
        )

        Operator.Add -> thisWeight + calculateTotal(expression.drop(1), defaultUOM)
        Operator.Subtract -> thisWeight - calculateTotal(expression.drop(1), defaultUOM)
    }
}
