package com.lift.bro.di

import com.lift.bro.data.LBDatabase
import com.lift.bro.presentation.Coordinator

expect class DependencyContainer {
    val database: LBDatabase

    val coordinator: Coordinator

    fun launchCalculator()
}

expect val dependencies: DependencyContainer