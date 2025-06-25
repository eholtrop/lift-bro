package com.lift.bro.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalUnitOfMeasure

internal val LBSet.formattedTempo: String get() = "${this.tempo.down}/${this.tempo.hold}/${this.tempo.up}"

internal val LBSet.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

@Composable
internal fun LBSet.prettyPrintSet(
    uom: UOM = LocalUnitOfMeasure.current,
): AnnotatedString = buildAnnotatedString {
    withStyle(
        style = SpanStyle(),
    ) { append("$reps x ${weight.decimalFormat()} ${uom.value}" + if (rpe != null) " at ${rpe}rpe" else "") }

    if (mer > 0 && LocalShowMERCalcs.current?.enabled == true) {
        withStyle(
            style = MaterialTheme.typography.labelMedium.toSpanStyle(),
        ) {
            append(" (+${mer}mer)")
        }
    }
}

@Composable
internal fun LBSet.formattedWeight(): String = "${this.weight.decimalFormat()} ${LocalUnitOfMeasure.current.value}"

internal val LBSet.formattedMax: String get() = "${this.reps} x ${this.formattedTempo}"