package com.lift.bro.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalCalculatorVisibility
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.Space
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.decimalFormat

enum class Digit(digit: Int) {
    One(1),
    Two(2),
    Three(3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
    Eight(8),
    Nine(9),
}

enum class Operator(
    val bedmasIndex: Int,
    val char: Char
) {
    Multiply(0, 'x'),
    Divide(0, '/'),
    Add(1, '+'),
    Subtract(1, '-'),
}

enum class Action {
    Backspace,
    Clear,
    Equals,
}

@Composable
fun WeightCalculatorBottomSheet(
    modifier: Modifier = Modifier,
) {
    val showCalculator = LocalCalculatorVisibility.current
    Box(
        modifier = modifier,
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = showCalculator.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable(
                        onClick = {
                            showCalculator.value = false
                        }
                    )
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .6f))
            )
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.BottomCenter),
            visible = showCalculator.value,
            enter = slideInVertically { it },
            exit = slideOutVertically { it } + fadeOut()
        ) {
            var weight by remember { mutableStateOf(0.0) }
            WeightCalculator(
                weight = weight,
                weightSubmitted = { weight = it ?: 0.0 }
            )
        }
    }
}

@Composable
fun WeightCalculator(
    modifier: Modifier = Modifier,
    weight: Double = 0.0,
    weightSubmitted: (Double?) -> Unit,
    defaultUOM: UOM = LocalUnitOfMeasure.current,
) {
    WeightCalculatorInternal(
        modifier = modifier,
        viewModel = CalculatorViewModel(
            initialState = CalculatorState(
                total = weight,
                expression = listOf(
                    Segment(
                        Weight(
                            value = weight,
                            uom = defaultUOM
                        )
                    )
                )
            ),
            defaultUOM = defaultUOM
        )
    )
}

@Composable
private fun WeightCalculatorInternal(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large.copy(
                bottomEnd = CornerSize(0.dp),
                bottomStart = CornerSize(0.dp)
            )
        ).clickable {

        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {


            Text(
                text = weightFormat(state.total),
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = state.expression.fold("") { acc, segment -> "$acc ${segment.weight.value.decimalFormat()} ${segment.weight.uom.value} ${segment.operation?.char ?: ""}" },
                style = MaterialTheme.typography.bodyLarge
            )
        }

        KeyPad(
            digitClicked = { viewModel.handleEvent(CalculatorEvent.DigitAdded(it)) },
            operatorClicked = { viewModel.handleEvent(CalculatorEvent.OperatorSelected(it)) },
            actionClicked = { viewModel.handleEvent(CalculatorEvent.ActionApplied(it)) }
        )

        Row {
            Button(
                onClick = {}
            ) {
                Text("Copy to Clipboard")
            }
            Space()
            Button(
                onClick = {}
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {}
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun KeyPad(
    digitClicked: (Int) -> Unit,
    operatorClicked: (Operator) -> Unit,
    actionClicked: (Action) -> Unit
) {
    Column {
        Row {
            CalculatorButton("1") {
                digitClicked(1)
            }
            CalculatorButton("2") {
                digitClicked(2)
            }
            CalculatorButton("3") {
                digitClicked(3)
            }
            CalculatorButton("/") {
                operatorClicked(Operator.Divide)
            }
        }
        Row {
            CalculatorButton("4") {
                digitClicked(4)
            }
            CalculatorButton("5") {
                digitClicked(5)
            }
            CalculatorButton("6") {
                digitClicked(6)
            }
            CalculatorButton("x") {
                operatorClicked(Operator.Multiply)
            }
        }
        Row {
            CalculatorButton("7") {
                digitClicked(7)
            }
            CalculatorButton("8") {
                digitClicked(8)
            }
            CalculatorButton("9") {
                digitClicked(9)
            }
            CalculatorButton("+") {
                operatorClicked(Operator.Add)
            }
        }
        Row {
            CalculatorButton(".") {
//                currentWeight = currentWeight * 10 + 1
            }
            CalculatorButton("0") {
                digitClicked(0)
            }
            CalculatorButton("<--") {
                actionClicked(Action.Backspace)
            }
            CalculatorButton("-") {
                operatorClicked(Operator.Subtract)
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick
    ) {
        Text(text)
    }
}