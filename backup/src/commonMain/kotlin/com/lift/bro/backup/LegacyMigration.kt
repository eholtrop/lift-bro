package com.lift.bro.backup

import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Workout

fun LegacyBackup.toBackup(): Backup {
    val variations = variations?.map { legacyVariation ->
        Movement(
            id = legacyVariation.id,
            lift = legacyVariation.lift,
            name = legacyVariation.name,
            reps = legacyVariation.reps,
            favourite = legacyVariation.favourite,
            notes = legacyVariation.notes,
            bodyWeight = legacyVariation.bodyWeight,
        )
    }

    val sets = sets?.map { lSet ->
        LBSet(
            id = lSet.id,
            movementId = lSet.variationId,
            weight = lSet.weight,
            reps = lSet.reps,
            tempo = lSet.tempo,
            date = lSet.date,
            notes = lSet.notes,
            rpe = lSet.rpe,
            mer = lSet.mer,
            bodyWeightRep = lSet.bodyWeightRep,
            videoUri = lSet.videoUri,
        )
    }

    val movementsById = variations?.associateBy { it.id } ?: emptyMap()
    val setsById = sets.orEmpty().associateBy { it.id }.toMutableMap()

    val workouts = workouts?.map { legacyWorkout ->
        val exercises = legacyWorkout.exercises.map { legacyExercise ->
            val sections = legacyExercise.variationSets.map { legacyVariationSet ->
                val resolvedSets = legacyVariationSet.sets.mapNotNull { set ->
                    setsById[set.id]?.copy(exerciseSectionId = legacyVariationSet.id)
                }
                resolvedSets.forEach { setsById[it.id] = it }

                Section(
                    id = legacyVariationSet.id,
                    exerciseId = legacyExercise.id,
                    primaryMovement = movementsById[legacyVariationSet.variation.id],
                )
            }
            Exercise(
                id = legacyExercise.id,
                workoutId = legacyWorkout.id,
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
        lifts = lifts,
        variations = variations,
        sets = setsById.values.toList(),
        liftingLogs = liftingLogs,
        workouts = workouts,
        exercises = null,
    )
}
