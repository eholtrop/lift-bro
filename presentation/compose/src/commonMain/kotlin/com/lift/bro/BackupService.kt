package com.lift.bro

import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.di.exerciseRepository
import com.lift.bro.di.liftRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Workout
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tv.dpal.logging.Log
import tv.dpal.logging.d

@Serializable
data class Backup(
    val lifts: List<Category>? = null,
    val variations: List<Movement>? = null,
    val sets: List<LBSet>? = null,
    val liftingLogs: List<LiftingLog>? = null,
    val workouts: List<Workout>? = null,
    val exercises: List<Exercise>? = null,
)

class RestoreUseCase(
    private val database: LBDatabase = dependencies.database,
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
            // apply the selected file
            withContext(Dispatchers.IO) {
                applyBackup(Json.decodeFromString<Backup>(readString()))
            }
            return true
        } ?: {
            Log.d(message = "error")
        }
        return false
    }

    private suspend fun applyBackup(backup: Backup) {
        // Delete existing data first
        database.clear()

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
            dependencies.database.logDataSource.save(
                id = it.id,
                notes = it.notes,
                date = it.date,
                vibe_check = it.vibe?.toLong(),
            )
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
