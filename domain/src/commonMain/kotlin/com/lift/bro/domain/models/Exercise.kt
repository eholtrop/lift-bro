package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

typealias ExerciseId = String

@Serializable
data class Exercise(
    val id: ExerciseId = uuid4().toString(),
    val workoutId: String,
    val sections: List<Section> = emptyList(),
) {
    val totalWeightMoved: Double = sections.sumOf { it.sets.sumOf { it.totalWeightMoved } }
}

typealias ExerciseSectionId = String

/**
 * @property id: the id for the section
 * @property exerciseId: the id for the parent exercise
 * @property sets: the sets associated with this section
 * @property movements: the movements associated with the sets in this section
 * @property recommendedSets: sets that have been recommended for this section (ex: from a copied previous workout!)
 */
@Serializable
data class Section(
    val id: ExerciseSectionId = uuid4().toString(),
    val exerciseId: ExerciseId,
    val sets: List<LBSet> = emptyList(),
    val movements: List<Movement> = emptyList(),
    val recommendedSets: List<LBSet> = emptyList(),
) {
    val movementSets = sets.map { set -> movements.first { it.id == set.variationId } to set }
}
