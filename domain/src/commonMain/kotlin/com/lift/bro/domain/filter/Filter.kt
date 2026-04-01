package com.lift.bro.domain.filter

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val id: String = uuid4().toString(),
    val name: String = "",
    val conditions: List<Condition>,
)

@Serializable
sealed interface Condition {
    @Serializable
    data class Min<T>(val field: Field<T>, val value: T) : Condition

    @Serializable
    data class Max<T>(val field: Field<T>, val value: T) : Condition

    @Serializable
    data class Equals<T>(val field: Field<T>, val value: T) : Condition
}

@Serializable
sealed class Field<T>(val name: String) {
    @Serializable
    data object Weight : Field<Double>("weight")

    @Serializable
    data object Reps : Field<Int>("reps")

    @Serializable
    data object RPE : Field<Int>("RPE")

    @Serializable
    data object TotalWeightMoved : Field<Double>("twm")

    @Serializable
    data object Variation : Field<String>("variationId")

    @Serializable
    data object Tempo : Field<Tempo>("tempo")
}
