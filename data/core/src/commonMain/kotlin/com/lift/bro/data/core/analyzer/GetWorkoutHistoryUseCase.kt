package com.lift.bro.data.core.analyzer

import com.lift.bro.domain.models.ExerciseHistoryEntry
import com.lift.bro.domain.models.HistorySummary
import com.lift.bro.domain.models.MuscleGroup
import com.lift.bro.domain.models.PersonalRecord
import com.lift.bro.domain.models.WorkoutHistory
import com.lift.bro.domain.models.WorkoutSummary
import com.lift.bro.domain.models.emptyWorkoutHistory
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import kotlinx.coroutines.flow.first

class GetWorkoutHistoryUseCase(
    private val setRepository: ISetRepository,
    private val variationRepository: IVariationRepository,
) {
    suspend operator fun invoke(daysBack: Int = 30): WorkoutHistory {
        val allSets = setRepository.listenAll(
            startDate = null,
            endDate = null,
            sorting = com.lift.bro.domain.repositories.Sorting.date,
            order = com.lift.bro.domain.repositories.Order.Descending
        ).first()

        if (allSets.isEmpty()) {
            return emptyWorkoutHistory()
        }

        val variations = variationRepository.getAll().associateBy { it.id }

        val exerciseHistoryMap = allSets.groupBy { it.variationId }
        val exerciseHistory = exerciseHistoryMap.map { (variationId, variationSets) ->
            val variation = variations[variationId]
            val bestSet = variationSets.maxByOrNull { calculateMax(it.reps.toInt(), it.weight) }

            ExerciseHistoryEntry(
                variationId = variationId,
                variationName = variation?.name ?: "Unknown",
                liftName = variation?.lift?.name ?: "Unknown",
                muscleGroup = deriveMuscleGroup(variation?.lift?.name),
                totalSessions = variationSets
                    .map { it.date.toString().take(10) }
                    .distinct()
                    .size,
                bestWeight = bestSet?.weight ?: 0.0,
                bestReps = bestSet?.reps ?: 0,
                bestEstimatedMax = bestSet?.let { calculateMax(it.reps.toInt(), it.weight) } ?: 0.0,
                averageWeight = variationSets.map { it.weight }.average(),
                averageReps = variationSets.map { it.reps }.average(),
                lastPerformed = variationSets.maxByOrNull { it.date }?.date?.toString()?.take(10) ?: "",
                frequencyPerWeek = variationSets.size.toDouble() / (daysBack / 7.0),
            )
        }

        val totalVolume = allSets.sumOf { it.weight * it.reps }
        val totalWorkouts = allSets
            .map { it.date.toString().take(10) }
            .distinct()
            .size

        val recentWorkouts = allSets
            .groupBy { it.date.toString().take(10) }
            .map { (date, dateSets) ->
                WorkoutSummary(
                    id = date,
                    date = date,
                    muscleGroupsWorked = listOf(deriveMuscleGroup(
                        variations[dateSets.firstOrNull()?.variationId]?.lift?.name
                    )),
                    totalExercises = dateSets.map { it.variationId }.distinct().size,
                    totalVolume = dateSets.sumOf { it.weight * it.reps },
                )
            }
            .sortedByDescending { it.date }
            .take(10)

        val prs = exerciseHistory
            .sortedByDescending { it.bestEstimatedMax }
            .take(5)
            .map { entry ->
                PersonalRecord(
                    liftName = entry.liftName,
                    variationName = entry.variationName,
                    weight = entry.bestWeight,
                    reps = entry.bestReps,
                    estimatedMax = entry.bestEstimatedMax,
                    date = entry.lastPerformed,
                )
            }

        val summary = HistorySummary(
            totalWorkouts = totalWorkouts,
            totalVolume = totalVolume,
            mostConsistentMuscleGroups = exerciseHistory
                .groupBy { it.muscleGroup }
                .mapValues { it.value.sumOf { e -> e.frequencyPerWeek } }
                .toList()
                .sortedByDescending { it.second }
                .take(3)
                .map { it.first },
            leastConsistentMuscleGroups = MuscleGroup.entries.toList()
                .filter { mg -> exerciseHistory.none { it.muscleGroup == mg } },
            averageWorkoutsPerWeek = totalWorkouts.toDouble() / (daysBack / 7.0),
            currentStreak = 0,
            personalRecords = prs,
        )

        return WorkoutHistory(
            summary = summary,
            exerciseHistory = exerciseHistory,
            recentWorkouts = recentWorkouts,
        )
    }

    private fun calculateMax(reps: Int, weight: Double): Double {
        return if (reps == 1) weight else weight * (1 + (reps / 30.0))
    }

    private fun deriveMuscleGroup(liftName: String?): MuscleGroup {
        val name = liftName?.lowercase() ?: return MuscleGroup.FullBody
        return when {
            isIn(name, "chest", "bench", "fly") -> MuscleGroup.Chest
            isIn(name, "back", "row", "pull", "deadlift") -> MuscleGroup.Back
            isIn(name, "shoulder", "press", "raise", "delts") -> MuscleGroup.Shoulders
            isIn(name, "bicep", "curl") && !isIn(name, "tricep") -> MuscleGroup.Biceps
            isIn(name, "tricep", "pushdown", "dip") -> MuscleGroup.Triceps
            isIn(name, "leg", "squat", "calf") -> MuscleGroup.Legs
            isIn(name, "core", "plank", "crunch") -> MuscleGroup.Core
            else -> MuscleGroup.FullBody
        }
    }

    private fun isIn(name: String, vararg keywords: String) = keywords.any { name.contains(it) }
}
