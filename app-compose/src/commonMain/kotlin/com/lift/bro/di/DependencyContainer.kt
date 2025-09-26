package com.lift.bro.di

import com.lift.bro.data.LBDatabase
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.data.core.repository.SetRepository
import com.lift.bro.data.sqldelight.datasource.SetLocalDataSourceImpl
import com.lift.bro.data.core.repository.VariationRepository
import com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource
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

val DependencyContainer.setRepository: ISetRepository get() =
    SetRepository(
        local = SetLocalDataSourceImpl(
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

expect val dependencies: DependencyContainer
