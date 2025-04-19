package com.lift.bro.presentation.navigation

import androidx.compose.runtime.compositionLocalOf

val LocalNavCoordinator = compositionLocalOf<NavCoordinator>() {
    error("NavHostController was not set")
}