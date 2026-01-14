package com.lift.bro.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.UOM
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.Log
import com.lift.bro.presentation.d
import com.lift.bro.ui.weightFormat

@Composable
internal fun LBSet.prettyPrintSet(
    uom: UOM = LocalUnitOfMeasure.current,
    enableTwm: Boolean = LocalTwmSettings.current,
    enableMers: Boolean = LocalShowMERCalcs.current?.enabled == true
): AnnotatedString = buildAnnotatedString {
    withStyle(
        style = SpanStyle(),
    ) {
        Log.d(message = bodyWeightRep.toString())
        if (bodyWeightRep == true) {
            append("$reps x bw")
            if (weight > 0.0) {
                append(" + ${weightFormat(weight, uom = uom)}")
            }
        } else {
            append("$reps x ${weightFormat(weight, uom = uom)}")
        }
    }
    if (enableTwm && totalWeightMoved > 0.0) {
        withStyle(
            style = MaterialTheme.typography.labelMedium.toSpanStyle(),
        ) {
            append(" (${totalWeightMoved.decimalFormat()})")
        }
    }
    if (rpe != null) {
        withStyle(
            style = SpanStyle(),
        ) {
            append(" at ${rpe}rpe")
        }
    }

    if (mer > 0 && enableMers) {
        withStyle(
            style = MaterialTheme.typography.labelMedium.toSpanStyle(),
        ) {
            append(" (+${mer}mer)")
        }
    }
}
