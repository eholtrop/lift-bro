package com.lift.bro.domain.models

data class Metric(
    val value: Pair<Double, Double>,
    val type: MetricType,
    val scope: MetricScope,
    val interval: MetricInterval,
)

enum class MetricType {
    Weight,
    Reps,
    Streak,
    RPE,
    OneRepMax,
}

enum class MetricScope {
    Lift,
    Variation,
    Set,
}

enum class MetricInterval {
    Week,
    Month,
    Year,
}
