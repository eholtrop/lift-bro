package com.lift.bro.ui.calculator

import androidx.annotation.Size
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.models.calculateMax
import com.lift.bro.presentation.LocalCalculatorVisibility
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.home.concernedIconRes
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.AnimatedText
import com.lift.bro.ui.AnimatedTextDefaults
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import kotlinx.coroutines.delay
import kotlinx.io.discardingSink
import org.jetbrains.compose.resources.painterResource

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
    var total by remember { mutableStateOf(0.0) }

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
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Space(modifier = Modifier.animateContentSize())

            Box(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                var includeBarBell by rememberSaveable { mutableStateOf(true) }

                CalculatorBarBell(
                    visible = showCalculator.value,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    var localTotal =
                        (total - if (includeBarBell) 45 else 0) / 2 // subtract the weight of the bar

                    AnimatedVisibility(
                        visible = showCalculator.value,
                        enter = slideInHorizontally { -it } + fadeIn(),
                        exit = slideOutHorizontally { -it } + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    onClick = { includeBarBell = !includeBarBell },
                                    role = Role.Checkbox,
                                )
                                .minimumInteractiveComponentSize()
                                .padding(start = MaterialTheme.spacing.half),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Checkbox(
//                                modifier = Modifier.size(24.dp),
//                                checked = includeBarBell,
//                                onCheckedChange = { includeBarBell = it }
//                            )
                            TriStateCheckbox(
                                state = ToggleableState(includeBarBell),
                                onClick = null
                            )
                            Text(
                                modifier = Modifier.padding(start = MaterialTheme.spacing.half),
                                text = "45 ${LocalUnitOfMeasure.current.value}",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Space(MaterialTheme.spacing.half)
                            Box(
                                modifier = Modifier.background(
                                    color = Color.DarkGray,
                                    shape = MaterialTheme.shapes.extraSmall
                                ).size(16.dp, 36.dp)
                            )
                        }
                    }

                    val plates = listOf(
                        Plate(45.0, UOM.POUNDS, MaterialTheme.colorScheme.primary),
                        Plate(35.0, UOM.POUNDS, MaterialTheme.colorScheme.secondary),
                        Plate(25.0, UOM.POUNDS, MaterialTheme.colorScheme.tertiary),
                        Plate(10.0, UOM.POUNDS, MaterialTheme.colorScheme.primary),
                        Plate(5.0, UOM.POUNDS, MaterialTheme.colorScheme.secondary),
                        Plate(2.5, UOM.POUNDS, MaterialTheme.colorScheme.tertiary),
                        Plate(1.0, UOM.POUNDS, MaterialTheme.colorScheme.primary),
                    )

                    plates.sortedByDescending { it.weight }.forEach { plate ->
                        val numPlates = (localTotal / plate.weight).toInt()
                        localTotal = localTotal % plate.weight

                        repeat(numPlates) { index ->
                            var showPlate by remember { mutableStateOf(false) }

                            AnimatedVisibility(
                                visible = showPlate && showCalculator.value,
                                enter = slideInHorizontally(animationSpec = tween(easing = EaseIn)) { it * 5 },
                                exit = slideOutHorizontally(tween(easing = EaseOut)) { it } + fadeOut()
                            ) {
                                PlateBox(
                                    plate = plate
                                )
                            }

                            LaunchedEffect(Unit) {
                                delay(50L * index)
                                showPlate = true
                            }
                        }
                    }
                }
            }

            Space(MaterialTheme.spacing.one)

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.CenterHorizontally).animateContentSize(),
                visible = total > 1000,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Surface(
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.one),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Row(
                        modifier = Modifier.padding(all = MaterialTheme.spacing.half),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            modifier = Modifier.size(LocalTextStyle.current.lineHeight.value.dp),
                            painter = painterResource(LocalLiftBro.current.iconRes()),
                            contentDescription = null,
                        )
                        Space(MaterialTheme.spacing.half)
                        Text(
                            text = "We're gonna need a bigger bar... 🧔‍♂️"
                        )
                    }
                }

                Space(MaterialTheme.spacing.one)
            }

            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = showCalculator.value,
                enter = slideInVertically { it },
                exit = slideOutVertically { it } + fadeOut()
            ) {
                WeightCalculator(
                    weightSubmitted = { total = it ?: 0.0 }
                )
            }
        }
    }
}

data class Plate(
    val weight: Double,
    val uom: UOM,
    val color: Color,
)

@Composable
fun PlateBox(
    modifier: Modifier = Modifier,
    plate: Plate
) {
    Box(
        modifier = modifier.defaultMinSize(minHeight = 48.dp).size(
            width = 36.dp,
            height = ((plate.weight / 45.0) * 256).dp
        )
            .background(
                color = plate.color,
                shape = MaterialTheme.shapes.small,
            ).border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = MaterialTheme.shapes.small,
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleSmall,
            ) {
                Text(text = plate.weight.decimalFormat())
//                if (LocalUnitOfMeasure.current != plate.uom) {
                Text(text = LocalUnitOfMeasure.current.value)
//                }
            }
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
    val interactor = rememberInteractor(
        initialState = CalculatorState(
            total = weight.decimalFormat(),
            expression = listOf(
                Segment(
                    Weight(
                        value = weight,
                        uom = defaultUOM
                    )
                )
            )
        ),
        reducers = calculatorReducers(defaultUOM),
    )

    val state by interactor.state.collectAsState()

    WeightCalculatorInternal(
        modifier = modifier,
        state = state,
        weightChanged = weightSubmitted,
        dispatcher = { interactor(it) }
    )
}

@Composable
private fun WeightCalculatorInternal(
    modifier: Modifier = Modifier,
    state: CalculatorState,
    dispatcher: (CalculatorEvent) -> Unit,
    weightChanged: (Double) -> Unit = {}
) {

    LaunchedEffect(state.total) {
        state.total.toDoubleOrNull()?.let {
            weightChanged(it)
        } ?: run {

        }
    }

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
            Row {
                AnimatedText(
                    text = state.total.toDoubleOrNull()?.let { weightFormat(it) }
                        ?: run { state.total },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    transitionForChar = { char, index ->
                        if (char.isDigit()) {
                            AnimatedTextDefaults.transitionForChar(this, char, index)
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    }
                )

                AnimatedVisibility(
                    visible = state.total.toDoubleOrNull() == null,
                    enter = fadeIn(),
                    exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.CenterEnd),
                ) {
                    Space(MaterialTheme.spacing.half)
                    Image(
                        modifier = Modifier.size(MaterialTheme.typography.headlineLarge.lineHeight.value.dp),
                        painter = painterResource(LocalLiftBro.current.concernedIconRes()),
                        contentDescription = null
                    )
                }
            }

            Text(
                text = buildAnnotatedString {
                    state.expression.forEachIndexed { index, segment ->
                        withStyle(MaterialTheme.typography.titleLarge.toSpanStyle()) {
                            if (index != 0) {
                                append(" ")
                            }
                            append(segment.weight.value.decimalFormat(segment.decimalApplied))
                        }

                        withLink(
                            link = LinkAnnotation.Clickable(
                                tag = segment.toString(),
                                styles = TextLinkStyles(
                                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.secondary)
                                        .toSpanStyle(),
                                    pressedStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.tertiary)
                                        .toSpanStyle(),
                                    hoveredStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary)
                                        .toSpanStyle(),
                                )
                            ) {
                                dispatcher(CalculatorEvent.ToggleUOMForIndex(index))
                            }
                        ) {
                            if (state.expression.getOrNull(index - 1)?.operation != Operator.Multiply) {
                                append("\u00A0")
                                append(segment.weight.uom.value)
                            }
                        }

                        segment.operation?.let {
                            withStyle(MaterialTheme.typography.titleLarge.toSpanStyle()) {
                                append(" ")
                                append(it.char)
                            }
                        }
                    }
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        KeyPad(
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.one),
            digitClicked = { dispatcher(CalculatorEvent.DigitAdded(it)) },
            operatorClicked = { dispatcher(CalculatorEvent.OperatorSelected(it)) },
            actionClicked = { dispatcher(CalculatorEvent.ActionApplied(it)) },
            decimalClicked = { dispatcher(CalculatorEvent.ActionApplied(Action.Decimal)) }
        )
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
    // 0 is always at the bottom
    val digits = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).reversed()

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        digits.chunked(3).forEachIndexed { index, row ->

            Row(
                horizontalArrangement = horizontalArrangement
            ) {
                row.sortedBy { it }.forEach {
                    CalculatorButton(
                        modifier = calculatorButtonModifier(),
                        text = it.toString()
                    ) {
                        digitClicked(it)
                    }
                }
                when (index) {
                    0 -> CalculatorButton(
                        modifier = calculatorButtonModifier(),
                        "/"
                    ) {
                        operatorClicked(Operator.Divide)
                    }

                    1 ->
                        CalculatorButton(
                            modifier = calculatorButtonModifier(),
                            "x"
                        ) {
                            operatorClicked(Operator.Multiply)
                        }

                    2 ->
                        CalculatorButton(
                            modifier = calculatorButtonModifier(),
                            "+"
                        ) {
                            operatorClicked(Operator.Add)
                        }
                }
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