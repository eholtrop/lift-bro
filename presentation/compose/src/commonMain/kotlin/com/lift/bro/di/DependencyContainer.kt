package com.lift.bro.di

import com.lift.bro.data.LBDatabase
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.data.core.repository.SetRepository
import com.lift.bro.data.core.repository.LiftRepository
import com.lift.bro.data.core.repository.ExerciseRepository
import com.lift.bro.data.core.repository.GoalRepository
import com.lift.bro.data.sqldelight.datasource.SqldelightLiftDataSource
import com.lift.bro.data.sqldelight.datasource.SqldelightSetDataSource
import com.lift.bro.data.sqldelight.datasource.SqldelightExerciseDataSource
import com.lift.bro.data.core.repository.VariationRepository
import com.lift.bro.data.sqldelight.datasource.SqlDelightGoalDataSource
import com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.IGoalRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository

expect class DependencyContainer {
    val database: LBDatabase

    val settingsRepository: ISettingsRepository

    fun launchUrl(url: String)

    fun launchManageSubscriptions()
}

val DependencyContainer.setRepository: ISetRepository get() =
SetRepository(
        local = SqldelightSetDataSource(
            setQueries = database.setQueries,
        )
    )

val DependencyContainer.variationRepository: IVariationRepository get() =
    VariationRepository(
        local = SqlDelightVariationDataSource(
            liftQueries = database.liftQueries,
            setQueries = database.setQueries,
            variationQueries = database.variationQueries,
        )
    )

val DependencyContainer.workoutRepository: IWorkoutRepository get() = WorkoutRepository(database)

val DependencyContainer.liftRepository: ILiftRepository get() =
    LiftRepository(
        local = SqldelightLiftDataSource(
            liftQueries = database.liftQueries,
        )
    )

val DependencyContainer.exerciseRepository: IExerciseRepository get() =
    ExerciseRepository(
        local = SqldelightExerciseDataSource(
            exerciseQueries = database.exerciseQueries,
            setQueries = database.setQueries,
            variationQueries = database.variationQueries,
        )
    )

val DependencyContainer.goalsRepository: IGoalRepository get() = GoalRepository(
    goalDataSource = SqlDelightGoalDataSource(
        goalQueries = database.goalQueries
    )
)

expect val dependencies: DependencyContainer
