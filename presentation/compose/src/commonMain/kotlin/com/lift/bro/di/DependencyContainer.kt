package com.lift.bro.di

import com.lift.bro.audio.AudioPlayer
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.client.LiftBroClientConfig
import com.lift.bro.data.client.createLiftBroClient
import com.lift.bro.data.client.datasources.KtorGoalRepository
import com.lift.bro.data.client.datasources.KtorLiftDataSource
import com.lift.bro.data.client.datasources.KtorSetDataSource
import com.lift.bro.data.client.datasources.KtorVariationDataSource
import com.lift.bro.data.core.repository.ExerciseRepository
import com.lift.bro.data.core.repository.GoalRepository
import com.lift.bro.data.core.repository.LiftRepository
import com.lift.bro.data.core.repository.SetRepository
import com.lift.bro.data.core.repository.VariationRepository
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.data.sqldelight.datasource.SqlDelightGoalDataSource
import com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource
import com.lift.bro.data.sqldelight.datasource.SqldelightExerciseDataSource
import com.lift.bro.data.sqldelight.datasource.SqldelightLiftDataSource
import com.lift.bro.data.sqldelight.datasource.SqldelightSetDataSource
import com.lift.bro.data.video.VideoStorage
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.IGoalRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository

expect class DependencyContainer {
    val database: LBDatabase

    val settingsRepository: ISettingsRepository

    fun launchUrl(url: String)

    fun launchManageSubscriptions()

    val audioPlayer: AudioPlayer

    val videoStorage: VideoStorage
}

private val remoteUrl: String?
    get() {
        return dependencies.settingsRepository.getClientUrl()
    }

val DependencyContainer.setRepository: ISetRepository
    get() =
        SetRepository(
            local = if (remoteUrl == null) {
                SqldelightSetDataSource(
                    setQueries = database.setQueries,
                )
            } else {
                KtorSetDataSource(
                    createLiftBroClient(config = LiftBroClientConfig(baseUrl = remoteUrl!!))
                )
            }
        )

val DependencyContainer.localSetRepository: ISetRepository
    get() =
        SetRepository(
            local = SqldelightSetDataSource(
                setQueries = database.setQueries
            )
        )

val DependencyContainer.variationRepository: IVariationRepository
    get() =
        VariationRepository(
            local = if (remoteUrl == null) {
                SqlDelightVariationDataSource(
                    liftQueries = database.liftQueries,
                    setQueries = database.setQueries,
                    variationQueries = database.variationQueries,
                )
            } else {
                KtorVariationDataSource(
                    createLiftBroClient(config = LiftBroClientConfig(baseUrl = remoteUrl!!))
                )
            }
        )
val DependencyContainer.localVariationRepository: IVariationRepository
    get() =
        VariationRepository(
            local = SqlDelightVariationDataSource(
                liftQueries = database.liftQueries,
                setQueries = database.setQueries,
                variationQueries = database.variationQueries,
            )
        )

val DependencyContainer.workoutRepository: IWorkoutRepository get() = WorkoutRepository(database)

val DependencyContainer.liftRepository: ILiftRepository
    get() =
        LiftRepository(
            local = if (remoteUrl == null) {
                SqldelightLiftDataSource(
                    liftQueries = database.liftQueries,
                )
            } else {
                KtorLiftDataSource(
                    createLiftBroClient(config = LiftBroClientConfig(baseUrl = remoteUrl!!))
                )
            }
        )

val DependencyContainer.localLiftRepository: ILiftRepository
    get() = LiftRepository(
        SqldelightLiftDataSource(
            liftQueries = database.liftQueries,
        )
    )

val DependencyContainer.exerciseRepository: IExerciseRepository
    get() =
        ExerciseRepository(
            local = SqldelightExerciseDataSource(
                exerciseQueries = database.exerciseQueries,
                setQueries = database.setQueries,
                variationQueries = database.variationQueries,
            )
        )

val DependencyContainer.goalsRepository: IGoalRepository
    get() = if (remoteUrl == null) {
        GoalRepository(
            goalDataSource = SqlDelightGoalDataSource(
                goalQueries = database.goalQueries
            )
        )
    } else {
        KtorGoalRepository(baseUrl = remoteUrl!!)
    }

val DependencyContainer.localGoalsRepository: IGoalRepository
    get() = GoalRepository(
        goalDataSource = SqlDelightGoalDataSource(
            goalQueries = database.goalQueries
        )
    )

expect val dependencies: DependencyContainer
