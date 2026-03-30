package com.lift.bro.di

import androidx.compose.runtime.compositionLocalOf

val LocalDependencies = compositionLocalOf<DependencyContainer> {
    error(
        "DependencyContainer not provided."
    )
}
