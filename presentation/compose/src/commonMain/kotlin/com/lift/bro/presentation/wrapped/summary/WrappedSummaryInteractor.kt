package com.lift.bro.presentation.wrapped.summary

import androidx.compose.runtime.Composable
import com.lift.bro.domain.models.Goal
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.wrapped.HeavyThing
import kotlinx.serialization.Serializable

//@Composable
//fun rememberWrappedSummaryState() = rememberInteractor<Interactor<WrappedSummaryState, WrappedSummaryEvents>(
//    initialState = WrappedSummaryState(),
//    source = {
//
//    }
//)

sealed interface WrappedSummaryEvents

@Serializable
data class WrappedSummaryState(
    val weight: WrappedSummaryWeightState,
    val consistencies: List<WrappedSummaryConsistencyState>,
    val progression: List<WrappedSummaryProgressState>,
    val goals: List<Goal>,
)

@Serializable
data class WrappedSummaryWeightState(
    val totalWeightMoved: Double,
    val heaviestVariationWeight: Double,
    val heaviestVariationName: String,
    val numOfHeavyThings: Int,
    val heavyThing: HeavyThing,
)

@Serializable
data class WrappedSummaryRepsState(
    val totalReps: Int,
    val variationReps: Int,
    val variationName: String,
    val repsPerDay: Int,
)

@Serializable
data class WrappedSummaryConsistencyState(
    val title: String,
    val occurrences: Int,
)

@Serializable
data class WrappedSummaryProgressState(
    val title: String,
    val progress: Double,
    val minWeight: Double,
    val maxWeight: Double,
)
