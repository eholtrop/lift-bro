package com.lift.bro.presentation.home

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun EmptyHomeScreenPreview() {
    AppTheme {
        EmptyHomeScreen(
            addLiftClicked = {},
            loadDefaultLifts = {}
        )
    }
}