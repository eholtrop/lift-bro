package com.lift.bro.utils

import androidx.compose.runtime.Composable
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.LocalUnitOfMeasure

internal val LBSet.formattedTempo: String get() = "${this.tempo.down}/${this.tempo.hold}/${this.tempo.up}"

internal val LBSet.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

@Composable
internal fun LBSet.formattedWeight(): String = "${this.weight.decimalFormat()} ${LocalUnitOfMeasure.current.value}"

internal val LBSet.formattedMax: String get() = "${this.reps} x ${this.formattedTempo}"