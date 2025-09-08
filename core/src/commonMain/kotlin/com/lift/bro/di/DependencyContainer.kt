package com.lift.bro.di

import androidx.compose.runtime.compositionLocalOf
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.repository.ExerciseRepository
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository

expect class DependencyContainer {
    val database: LBDatabase

    val settingsRepository: ISettingsRepository

    fun launchCalculator()

    fun launchUrl(url: String)

    fun launchManageSubscriptions()
}

val DependencyContainer.setRepository: ISetRepository get() = database.setDataSource

val DependencyContainer.exerciseRepository: IExerciseRepository get() = ExerciseRepository()

val DependencyContainer.variationRepository: IVariationRepository get() = database.variantDataSource

val DependencyContainer.workoutRepository: IWorkoutRepository get() = WorkoutRepository(database)

expect val dependencies: DependencyContainer