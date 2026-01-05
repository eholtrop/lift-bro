package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.lift.bro.ui.calculator.WeightCalculator
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CalculatorPreview(isDarkMode: Boolean) {
    PreviewAppTheme(
        isDarkMode = isDarkMode
    ) {
        WeightCalculator(
            weight = 14.0,
            weightSubmitted = {}
        )
    }
}

@Preview
@Composable
fun CalculatorPreview_Light() {
    CalculatorPreview(false)
}

@Preview
@Composable
fun CalculatorPreview_Dark() {
    CalculatorPreview(true)
}
