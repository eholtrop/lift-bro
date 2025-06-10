package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.presentation.onboarding.OnboardingBroScreen
import com.lift.bro.presentation.onboarding.OnboardingSkipScreen
import com.lift.bro.presentation.onboarding.OnboardingSetupScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun OnboardingBroScreenPreview() {
    AppTheme {
        OnboardingBroScreen {}
    }
}

@Preview
@Composable
fun OnboardingUomScreenPreview() {
    AppTheme {
        OnboardingSetupScreen {}
    }
}

@Preview
@Composable
fun OnboardingSetupScreenPreview() {
    AppTheme {
        OnboardingSkipScreen(
            setupClicked = {},
            continueClicked = {}
        )
    }
}