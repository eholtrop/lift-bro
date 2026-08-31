package com.lift.bro

import com.lift.bro.backup.Backup
import com.lift.bro.backup.LegacyBackup
import com.lift.bro.backup.toBackup
import com.lift.bro.di.clearUserDataUseCase
import com.lift.bro.di.dependencies
import com.lift.bro.di.exerciseRepository
import com.lift.bro.di.liftRepository
import com.lift.bro.di.liftingLogRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tv.dpal.logging.Log
import tv.dpal.logging.d

class RestoreUseCase(
    private val clearUserDataUseCase: ClearUserDataUseCase = dependencies.clearUserDataUseCase,
    private val liftRepository: ILiftRepository = dependencies.liftRepository,
    private val variationRepository: IVariationRepository = dependencies.variationRepository,
    private val setRepository: ISetRepository = dependencies.setRepository,
    private val workoutRepository: IWorkoutRepository = dependencies.workoutRepository,
    private val exerciseRepository: IExerciseRepository = dependencies.exerciseRepository,
) {
    suspend operator fun invoke(): Boolean {
        // let the user pick a file to restore
        val backupDir = FileKit.cacheDir / "backups"
        if (!backupDir.exists()) {
            backupDir.createDirectories()
        }
        FileKit.openFilePicker(
            type = FileKitType.File("application/json"),
        )?.apply {
            Log.d(message = "file received")
            withContext(Dispatchers.IO) {
                val raw = readString()
                val backup = try {
                    Json.decodeFromString<Backup>(raw)
                } catch (_: Exception) {
                    Json.decodeFromString<LegacyBackup>(raw).toBackup()
                }
                applyBackup(backup)
            }
            return true
        } ?: {
            Log.d(message = "error")
        }
        return false
    }

    private suspend fun applyBackup(backup: Backup) {
        // Delete existing data first
        clearUserDataUseCase()

        backup.sets?.forEach {
            setRepository.save(it)
        }

        backup.variations?.forEach {
            variationRepository.save(variation = it)
        }

        backup.lifts?.forEach {
            liftRepository.save(it)
        }

        backup.liftingLogs?.forEach {
            dependencies.liftingLogRepository.save(it)
        }

        // Restore workouts
        backup.workouts?.forEach { workout ->
            workoutRepository.save(workout)
        }

        // Restore exercises
        backup.exercises?.forEach { exercise ->
            exerciseRepository.save(exercise)
        }
    }
}
