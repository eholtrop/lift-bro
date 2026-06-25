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
import com.lift.bro.domain.models.Section
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
import kotlinx.datetime.LocalDate
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

@Serializable
data class LegacyVariation(
    val id: String,
    val lift: String? = null,
    val name: String? = null,
    val reps: Long = 1,
    val favourite: Boolean = false,
    val notes: String? = null,
    val eMax: LBSet? = null,
    val oneRepMax: LBSet? = null,
    val maxReps: LBSet? = null,
    val latestSet: LBSet? = null,
    val bodyWeight: Boolean? = false,
)

@Serializable
data class LegacyVariationSet(
    val id: String,
    val exerciseId: String,
    val sets: List<String> = emptyList(),
    val movements: List<String> = emptyList(),
)

@Serializable
data class LegacyExercise(
    val id: String,
    val workoutId: String,
    val sections: List<LegacyVariationSet> = emptyList(),
)

@Serializable
data class LegacyWorkout(
    val id: String,
    val date: LocalDate,
    val warmup: String? = null,
    val exercises: List<LegacyExercise> = emptyList(),
    val finisher: String? = null,
)

@Serializable
data class LegacyBackup(
    val lifts: List<Category>? = null,
    val variations: List<LegacyVariation>? = null,
    val sets: List<LBSet>? = null,
    val liftingLogs: List<LiftingLog>? = null,
    val workouts: List<LegacyWorkout>? = null,
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
            withContext(Dispatchers.IO) {
                val raw = readString()
                val backup = try {
                    Json.decodeFromString<Backup>(raw)
                } catch (_: Exception) {
                    Json.decodeFromString<LegacyBackup>(raw).let { migrateLegacy(it) }
                }
                applyBackup(backup)
            }
            return true
        } ?: {
            Log.d(message = "error")
        }
        return false
    }

    private fun migrateLegacy(legacy: LegacyBackup): Backup {
        val liftsById = legacy.lifts?.associateBy { it.id } ?: emptyMap()

        val variations = legacy.variations?.map { legacyVariation ->
            Movement(
                id = legacyVariation.id,
                lift = legacyVariation.lift?.let { liftsById[it] },
                name = legacyVariation.name,
                reps = legacyVariation.reps,
                favourite = legacyVariation.favourite,
                notes = legacyVariation.notes,
                eMax = legacyVariation.eMax,
                oneRepMax = legacyVariation.oneRepMax,
                maxReps = legacyVariation.maxReps,
                latestSet = legacyVariation.latestSet,
                bodyWeight = legacyVariation.bodyWeight,
            )
        }

        val movementsById = variations?.associateBy { it.id } ?: emptyMap()
        val setsById = legacy.sets.orEmpty().associateBy { it.id }.toMutableMap()

        val workouts = legacy.workouts?.map { legacyWorkout ->
            val exercises = legacyWorkout.exercises.map { legacyExercise ->
                val sections = legacyExercise.sections.map { legacyVariationSet ->
                    val resolvedSets = legacyVariationSet.sets.mapNotNull { setId ->
                        setsById[setId]?.copy(exerciseSectionId = legacyVariationSet.id)
                    }
                    resolvedSets.forEach { setsById[it.id] = it }

                    Section(
                        id = legacyVariationSet.id,
                        exerciseId = legacyVariationSet.exerciseId,
                        sets = resolvedSets,
                        movements = legacyVariationSet.movements.mapNotNull { movId ->
                            movementsById[movId]
                        },
                        recommendedSets = emptyList()
                    )
                }
                Exercise(
                    id = legacyExercise.id,
                    workoutId = legacyExercise.workoutId,
                    sections = sections,
                )
            }
            Workout(
                id = legacyWorkout.id,
                date = legacyWorkout.date,
                warmup = legacyWorkout.warmup,
                exercises = exercises,
                finisher = legacyWorkout.finisher,
            )
        }

        return Backup(
            lifts = legacy.lifts,
            variations = variations,
            sets = setsById.values.toList(),
            liftingLogs = legacy.liftingLogs,
            workouts = workouts,
            exercises = null,
        )
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
