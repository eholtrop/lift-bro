package com.lift.bro.ui.calculator

import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.models.convert
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

data class CalculatorState(
    val total: Double,
    val expression: List<Segment>,
)

data class Segment(
    val weight: Weight,
    val operation: Operator? = null
)

data class Weight(
    val value: Double,
    val uom: UOM,
)

sealed class CalculatorEvent {
    data class DigitAdded(val digit: Int) : CalculatorEvent()
    data class OperatorSelected(val operation: Operator) : CalculatorEvent()
    data class ActionApplied(val action: Action) : CalculatorEvent()
}

class CalculatorViewModel(
    initialState: CalculatorState,
    private val defaultUOM: UOM,
) {

    private val inputs: Channel<CalculatorEvent> = Channel()

    fun handleEvent(event: CalculatorEvent) = inputs.trySend(event)

    val state = inputs.receiveAsFlow()
        .scan(initialState) { state, action ->
            when (action) {
                is CalculatorEvent.DigitAdded -> {
                    when (state.expression.last().operation == null) {
                        true -> {
                            val lastSegment = state.expression.last()
                            val newSegment = lastSegment.copy(
                                weight = Weight(
                                    lastSegment.weight.value * 10 + action.digit,
                                    lastSegment.weight.uom
                                )
                            )
                            state.copy(
                                expression = state.expression.subList(
                                    0,
                                    state.expression.lastIndex
                                ) + newSegment
                            )
                        }

                        false -> {
                            state.copy(
                                expression = state.expression + Segment(
                                    Weight(
                                        action.digit.toDouble(),
                                        UOM.POUNDS
                                    )
                                )
                            )
                        }
                    }
                }

                is CalculatorEvent.OperatorSelected -> {
                    val lastSegment = state.expression.last()
                    val newSegment = lastSegment.copy(
                        operation = action.operation
                    )
                    state.copy(
                        expression = state.expression.subList(
                            0,
                            state.expression.lastIndex
                        ) + newSegment
                    )
                }

                is CalculatorEvent.ActionApplied -> when (action.action) {
                    Action.Backspace -> {
                        val lastSegment = state.expression.last()
                        val newSegment = lastSegment.copy(
                            weight = Weight(
                                (lastSegment.weight.value * 10).toInt().toDouble() / 10,
                                lastSegment.weight.uom
                            )
                        )
                        state.copy(
                            expression = state.expression.subList(
                                0,
                                state.expression.lastIndex
                            ) + newSegment
                        )
                    }

                    Action.Clear -> state.copy(expression = emptyList())
                    Action.Equals -> state.copy(
                        expression = listOf(
                            Segment(
                                Weight(
                                    state.total,
                                    UOM.POUNDS
                                ),
                                null
                            )
                        )
                    )
                }
            }
        }.map { state ->
            state.copy(
                total = calculateTotal(state.expression)
            )
        }.stateIn(
            scope = GlobalScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = initialState
        )

    private fun calculateTotal(expression: List<Segment>): Double {
        if (expression.isEmpty()) return 0.0
        val segment = expression.first()
        val nextSegment = expression.getOrNull(1)

        val thisWeight = segment.weight.uom.convert(
            segment.weight.value,
            defaultUOM
        )
        val nextWeight = segment.weight.uom.convert(
            nextSegment?.weight?.value ?: 1.0,
            defaultUOM
        )

        return when (segment.operation) {
            null -> segment.weight.value
            Operator.Multiply -> thisWeight * nextWeight + calculateTotal(expression.drop(2))
            Operator.Divide -> thisWeight / nextWeight + calculateTotal(expression.drop(2))
            Operator.Add -> thisWeight + calculateTotal(expression.drop(1))
            Operator.Subtract -> thisWeight - calculateTotal(expression.drop(1))
        }
    }
}
