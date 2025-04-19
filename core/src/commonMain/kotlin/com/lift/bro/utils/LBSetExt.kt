package com.lift.bro.utils

import com.lift.bro.Settings
import com.lift.bro.domain.models.LBSet
import com.lift.bro.presentation.decimalFormat

internal val LBSet.formattedTempo: String get() = "${this.tempo.down}/${this.tempo.hold}/${this.tempo.up}"

internal val LBSet.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

internal val LBSet.formattedWeight: String get() = "${this.weight.decimalFormat()} ${Settings.defaultUOM.value}"

internal val LBSet.formattedMax: String get() = "${this.reps} x ${this.formattedTempo}"