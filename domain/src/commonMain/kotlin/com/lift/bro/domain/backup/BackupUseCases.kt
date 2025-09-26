package com.lift.bro.domain.backup

import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import kotlinx.coroutines.flow.first

class BackupUseCase(
    private val clockEpochMs: () -> Long,
    private val liftRepository: ILiftRepository,
    private val variationRepository: IVariationRepository,
    private val workoutRepository: IWorkoutRepository,
    private val fileDataSource: FileDataSource,
) {
    suspend operator fun invoke(target: BackupTarget): BackupDescriptor {
        val lifts = liftRepository.getAll()
        val variations = variationRepository.getAll()
        val workouts = workoutRepository.getAll().first()

        val snapshot = BackupSnapshot(
            lifts = lifts,
            variations = variations,
            workouts = workouts,
        )
        val descriptor = fileDataSource.write(target, snapshot)
        return descriptor.copy(createdAtEpochMs = clockEpochMs())
    }
}

class RestoreUseCase(
    private val exerciseRepository: IExerciseRepository,
    private val liftRepository: ILiftRepository,
    private val variationRepository: IVariationRepository,
    private val workoutRepository: IWorkoutRepository,
    private val fileDataSource: FileDataSource,
) {
    suspend operator fun invoke(source: RestoreSource): RestoreReport {
        val snapshot = fileDataSource.read(source)

        var inserted = 0
        var updated = 0

        // naive upsert strategy: try save; if entities pre-exist, repositories should replace/update
        snapshot.lifts.forEach { lift ->
            val wasInsert = liftRepository.save(lift)
            if (wasInsert) inserted++ else updated++
        }

        snapshot.variations.forEach { variation ->
            variationRepository.save(variation)
            // repository does not expose insert/update semantic; count as updated
            updated++
        }

        // Workouts aggregate contains exercises and sets; persist workouts then exercises
        snapshot.workouts.forEach { workout ->
            workoutRepository.save(workout)
            updated++
            workout.exercises.forEach { exercise ->
                exerciseRepository.save(exercise)
                updated++
            }
        }

        return RestoreReport(inserted = inserted, updated = updated, skipped = 0)
    }
}