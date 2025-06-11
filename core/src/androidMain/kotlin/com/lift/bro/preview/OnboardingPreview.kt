package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.presentation.onboarding.OnboardingBroScreen
import com.lift.bro.presentation.onboarding.OnboardingScreen
import com.lift.bro.presentation.onboarding.OnboardingSkipScreen
import com.lift.bro.presentation.onboarding.OnboardingSetupScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
fun OnboardingPage1ScreenPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        OnboardingScreen(0)
    }
}

@Preview
@Composable
fun OnboardingPage2ScreenPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        OnboardingScreen(1)
    }
}

@Preview
@Composable
fun OnboardingPage3ScreenPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        OnboardingScreen(2)
    }
}

@Preview
@Composable
fun OnboardingPage1ScreenPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        OnboardingScreen(0)
    }
}

@Preview
@Composable
fun OnboardingPage2ScreenPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        OnboardingScreen(1)
    }
}

@Preview
@Composable
fun OnboardingPage3ScreenPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        OnboardingScreen(2)
    }
}