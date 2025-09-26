package com.lift.bro.di

import com.lift.bro.data.LBDatabase
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.data.core.repository.SetRepository
import com.lift.bro.data.core.repository.LiftRepository
import com.lift.bro.data.core.repository.ExerciseRepository
import com.lift.bro.data.sqldelight.datasource.SqldelightLiftDataSource
import com.lift.bro.data.sqldelight.datasource.SqldelightSetDataSource
import com.lift.bro.data.sqldelight.datasource.SqldelightExerciseDataSource
import com.lift.bro.data.core.repository.VariationRepository
import com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.domain.backup.FileDataSource
import com.lift.bro.domain.backup.BackupUseCase
import com.lift.bro.domain.backup.RestoreUseCase
import com.lift.bro.domain.backup.BackupTarget
import com.lift.bro.domain.backup.RestoreSource
import com.lift.bro.data.file.JsonFileDataSource
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.div
import okio.Path.Companion.toPath
import kotlinx.datetime.Clock

expect class DependencyContainer {
    val database: LBDatabase

    val settingsRepository: ISettingsRepository

    fun launchCalculator()

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

// Backup/Restore DI
val DependencyContainer.fileDataSource: FileDataSource get() =
    JsonFileDataSource(rootDir = (FileKit.filesDir / "backups").toString().toPath())

val DependencyContainer.backupUseCase: BackupUseCase get() =
    BackupUseCase(
        clockEpochMs = { Clock.System.now().toEpochMilliseconds() },
        liftRepository = liftRepository,
        variationRepository = variationRepository,
        workoutRepository = workoutRepository,
        fileDataSource = fileDataSource,
    )

val DependencyContainer.restoreUseCase: RestoreUseCase get() =
    RestoreUseCase(
        exerciseRepository = exerciseRepository,
        liftRepository = liftRepository,
        variationRepository = variationRepository,
        workoutRepository = workoutRepository,
        fileDataSource = fileDataSource,
    )

expect val dependencies: DependencyContainer
