package com.lift.bro

import com.lift.bro.data.LBDatabase
import com.lift.bro.data.core.repository.SetRepository
import com.lift.bro.data.core.repository.VariationRepository
import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.di.exerciseRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.utils.toString
import com.lift.bro.utils.today
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.shareFile
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Backup(
    val lifts: List<Lift>? = null,
    val variations: List<Variation>? = null,
    val sets: List<LBSet>? = null,
    val liftingLogs: List<LiftingLog>? = null,
    val workouts: List<Workout>? = null,
    val exercises: List<Exercise>? = null,
)


/**
 * Does too many things but is WAY better than what was there before
 * (a static object that handled everything and fetched all dependencies)
 */
class BackupUseCase(
    private val liftRepository: ILiftRepository = dependencies.liftRepository,
    private val variationRepository: IVariationRepository = dependencies.variationRepository,
    private val setRepository: ISetRepository = dependencies.setRepository,
) {
    suspend operator fun invoke(backup: Backup? = null): Backup {
        // either use the provided backup or create one from the current DB
        return (backup ?: createBackup()).apply {

            // create required files in the file system
            val backupDir = FileKit.filesDir / "backups"
            if (!backupDir.exists()) {
                backupDir.createDirectories()
            }
            val backupFile = backupDir / "${Clock.System.now().toString("yyyy-MM-dd_HH:mm:ss")}.json"
            backupFile.writeString(Json.encodeToString(this))

            // force user to pick where the backup goes (should probably be somewhere else!)
            FileKit.shareFile(backupFile)

            // ensure last backup date is updated
            dependencies.settingsRepository.saveBackupSettings(BackupSettings(lastBackupDate = Clock.System.today))
        }
    }

    private suspend fun getAllWorkouts(): List<Workout> {
        // Get all workouts by using a very wide date range
        val startDate = LocalDate.fromEpochDays(0) // Very early date
        val endDate = LocalDate.fromEpochDays(999999) // Very far future date
        return dependencies.workoutRepository.getAll(startDate, endDate).first()
    }

    private suspend fun getAllExercises(): List<Exercise> {
        // Get all exercises by querying all workouts and extracting their exercises
        val workouts = getAllWorkouts()
        return workouts.flatMap { it.exercises }
    }

    private suspend fun createBackup(): Backup {
        return Backup(
            lifts = liftRepository.getAll(),
            variations = variationRepository.getAll(),
            sets = setRepository.listenAll().first(),
            liftingLogs = dependencies.database.logDataSource.getAll().executeAsList().map {
                LiftingLog(
                    id = it.id,
                    date = it.date,
                    notes = it.notes ?: "",
                    vibe = it.vibe_check?.toInt(),
                )
            },
            workouts = getAllWorkouts(),
            exercises = getAllExercises(),
        )
    }
}

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
        val backupDir = FileKit.filesDir / "backups"
        if (!backupDir.exists()) {
            backupDir.createDirectories()
        }
        FileKit.openFilePicker(
            type = FileKitType.File("application/json"),
            directory = backupDir
        )?.apply {
            // apply the selected file
            withContext(Dispatchers.IO) {
                applyBackup(Json.decodeFromString<Backup>(readString()))
            }
            return true
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

