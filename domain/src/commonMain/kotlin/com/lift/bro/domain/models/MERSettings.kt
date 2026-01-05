package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

/**
 * @param enabled: dictates whether the MER feature is enabled
 * @param threshold: the % threshold of a MER lift (defaults to 80, any fatigue above the threshold will count as a MER
 * @param weeklyTotalGoal: the number of MER's that you wish to achieve in a given week (total for ALL MER's)
 * @param weeklyVariationGoal: the number of MER's that you with to achieve for any given variation
 * @param weeklyLiftGoal: the number of MER's that you wish to achieve
 */
@Serializable
data class MERSettings(
    val enabled: Boolean = false,
    val threshold: Float = .8f,
    val weeklyTotalGoal: Int? = null,
    val weeklyVariationGoal: Int? = null,
    val weeklyLiftGoal: Int? = null,
)
