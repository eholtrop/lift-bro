package com.lift.bro.presentation.home

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.preview.DarkModeProvider
import com.lift.bro.preview.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
fun EmptyHomeScreenPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        EmptyHomeScreen(
            addLiftClicked = {},
            loadDefaultLifts = {}
        )
    }
}

@Preview
@Composable
fun EmptyHomeScreenPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        EmptyHomeScreen(
            addLiftClicked = {},
            loadDefaultLifts = {}
        )
    }
}