package com.lift.bro.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalCalculatorVisibility
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
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
    Decimal,
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
        ).navigationBarsPadding()
            .padding(
                top = MaterialTheme.spacing.one,
                start = MaterialTheme.spacing.half,
                end = MaterialTheme.spacing.half,
            )
        .clickable {

        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = weightFormat(state.total),
                style = MaterialTheme.typography.headlineLarge,
            )

            Text(
                text = state.expression.fold("") { acc, segment -> "$acc ${segment.weight.value.decimalFormat(segment.decimalApplied)} ${segment.weight.uom.value} ${segment.operation?.char ?: ""}" },
                style = MaterialTheme.typography.titleLarge
            )
        }

        KeyPad(
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.one),
            digitClicked = { viewModel.handleEvent(CalculatorEvent.DigitAdded(it)) },
            operatorClicked = { viewModel.handleEvent(CalculatorEvent.OperatorSelected(it)) },
            actionClicked = { viewModel.handleEvent(CalculatorEvent.ActionApplied(it)) },
            decimalClicked = { viewModel.handleEvent(CalculatorEvent.ActionApplied(Action.Decimal)) }
        )

        Row {
            Space()
            Button(
                onClick = {}
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {}
            ) {
                Text("Clipboard")
            }
        }
    }
}

@Composable
fun KeyPad(
    modifier: Modifier = Modifier,
    digitClicked: (Int) -> Unit,
    operatorClicked: (Operator) -> Unit,
    actionClicked: (Action) -> Unit,
    decimalClicked: () -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(MaterialTheme.spacing.half),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(MaterialTheme.spacing.half)
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        Row(
            horizontalArrangement = horizontalArrangement
        ) {
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                text = "1"
            ) {
                digitClicked(1)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "2"
            ) {
                digitClicked(2)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "3"
            ) {
                digitClicked(3)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "/"
            ) {
                operatorClicked(Operator.Divide)
            }
        }
        Row(
            horizontalArrangement = horizontalArrangement
        ) {
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "4"
            ) {
                digitClicked(4)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "5"
            ) {
                digitClicked(5)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "6"
            ) {
                digitClicked(6)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "x"
            ) {
                operatorClicked(Operator.Multiply)
            }
        }
        Row(
            horizontalArrangement = horizontalArrangement
        ) {
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "7"
            ) {
                digitClicked(7)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "8"
            ) {
                digitClicked(8)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "9"
            ) {
                digitClicked(9)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "+"
            ) {
                operatorClicked(Operator.Add)
            }
        }
        Row(
            horizontalArrangement = horizontalArrangement
        ) {
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "."
            ) {
                decimalClicked()
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "0"
            ) {
                digitClicked(0)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                vector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace"
            ) {
                actionClicked(Action.Backspace)
            }
            CalculatorButton(
                modifier = calculatorButtonModifier(),
                "-"
            ) {
                operatorClicked(Operator.Subtract)
            }
        }
    }
}

@Composable
fun RowScope.calculatorButtonModifier(): Modifier =
    Modifier.weight(1f)
        .aspectRatio(1f)

@Composable
fun CalculatorButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors()
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
@Composable
fun CalculatorButton(
    modifier: Modifier = Modifier,
    vector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors()
    ) {
        Icon(
            imageVector = vector,
            contentDescription = contentDescription
        )
    }
}