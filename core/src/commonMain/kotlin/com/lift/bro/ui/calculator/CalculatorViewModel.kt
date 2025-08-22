package com.lift.bro.ui.calculator

import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.models.convert
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
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
    val operation: Operator? = null,
    val decimalApplied: Boolean = false,
)

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
                    when (state.expression.lastOrNull()?.operation == null) {
                        true -> {
                            val lastSegment = state.expression.lastOrNull() ?: Segment(
                                Weight(0.0, defaultUOM),
                                null
                            )
                            val newWeight = when (lastSegment.decimalApplied) {
                                false -> lastSegment.weight.value * 10 + action.digit
                                true -> (lastSegment.weight.value.decimalFormat(true) + action.digit).toDouble()
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
                        expression = state.expression.dropLast(1) + newSegment
                    )
                }

                is CalculatorEvent.ActionApplied -> when (action.action) {
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
                            expression = state.expression.dropLast(1) + if (newSegment != null) listOf(
                                newSegment
                            ) else emptyList()
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
                                    ), decimalApplied = true
                                )
                            )
                        }
                    }
                }

                is CalculatorEvent.ToggleUOMForIndex -> {
                    val segment = state.expression[action.index]
                    val prefix = if (action.index == 0) emptyList() else state.expression.subList(
                        0,
                        action.index
                    )
                    val suffix =
                        if (state.expression.lastIndex == action.index) emptyList() else state.expression.subList(
                            action.index + 1,
                            state.expression.lastIndex + 1
                        )


                    state.copy(
                        expression = prefix + segment.copy(weight = segment.weight.let {
                            it.copy(
                                uom = it.uom.toggle()
                            )
                        }) + suffix
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
            null -> thisWeight
            Operator.Multiply -> calculateTotal(
                listOf(
                    Segment(
                        Weight(thisWeight * nextWeight, defaultUOM),
                        nextSegment?.operation
                    )
                ) + expression.drop(2)
            )

            Operator.Divide -> calculateTotal(
                listOf(
                    Segment(
                        Weight(thisWeight * nextWeight, defaultUOM),
                        nextSegment?.operation
                    )
                ) + expression.drop(2)
            )

            Operator.Add -> thisWeight + calculateTotal(expression.drop(1))
            Operator.Subtract -> thisWeight - calculateTotal(expression.drop(1))
        }
    }
}

private fun UOM.toggle(): UOM {
    return when (this) {
        UOM.KG -> UOM.POUNDS
        UOM.POUNDS -> UOM.KG
    }
}
