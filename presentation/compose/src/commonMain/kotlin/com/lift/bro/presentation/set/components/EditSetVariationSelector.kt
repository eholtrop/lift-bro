package com.lift.bro.presentation.set.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.set.EditSetState
import com.lift.bro.presentation.set.EditSetStateProvider
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.fullName
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.weight_selector_chin_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun EditSetVariationSelector(
    state: EditSetState?,
    showVariationDialog: () -> Unit,
) {
    val variation = state?.variation
    when {
        variation == null -> {
            Button(
                colors = ButtonDefaults.outlinedButtonColors(),
                onClick = {
                    showVariationDialog()
                }
            ) {
                Text(
                    "Select Variation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        else -> {
            Box(
                modifier = Modifier.padding(
                    vertical = MaterialTheme.spacing.one,
                    horizontal = MaterialTheme.spacing.one
                ).animateContentSize()
            ) {
                Column(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(
                            onClick = {
                                showVariationDialog()
                            },
                            role = Role.Button
                        )
                        .padding(
                            vertical = MaterialTheme.spacing.quarter,
                            horizontal = MaterialTheme.spacing.one
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = buildAnnotatedString {
                            with(variation) {
                                if (liftMaxPercentage == null && variationMaxPercentage == null) {
                                    withStyle(
                                        MaterialTheme.typography.titleMedium
                                            .copy(
                                                color = MaterialTheme.colorScheme.primary,
                                            ).toSpanStyle(),
                                    ) {
                                        append(variation.variation.fullName)
                                    }
                                } else {
                                    variationMaxPercentage?.let {
                                        append(
                                            buildSetLiftTitle(
                                                value = it.percentage,
                                                name = it.variationName
                                            )
                                        )
                                    }

                                    liftMaxPercentage?.let {
                                        if (variationMaxPercentage != null) {
                                            appendLine()
                                        }
                                        append(
                                            buildSetLiftTitle(
                                                value = it.percentage,
                                                name = it.variationName
                                            )
                                        )
                                    }
                                }

                                if (LocalTwmSettings.current) {
                                    appendLine()
                                    append("TWM: ${weightFormat((state.weight ?: 0.0) * (state.reps ?: 0))}")
                                }

                                if (LocalShowMERCalcs.current?.enabled == true) {
                                    appendLine()
                                    append("+${state.mers ?: 0} MER(s)")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun buildSetLiftTitle(
    value: Int,
    name: String,
): AnnotatedString {
    return buildAnnotatedString {
        val str = stringResource(
            Res.string.weight_selector_chin_title,
            value,
            name,
        )

        append(
            str.take(str.indexOf(name))
        )

        withStyle(
            MaterialTheme.typography.titleMedium
                .copy(
                    color = MaterialTheme.colorScheme.primary,
                ).toSpanStyle(),
        ) {
            append(
                name,
            )
        }

        append(
            str.substring(
                str.indexOf(name) + name.length,
            )
        )
    }
}

@Composable
@Preview
fun EditSetVariationSelectorPreview(@PreviewParameter(EditSetStateProvider::class) state: EditSetState) {
    PreviewAppTheme(isDarkMode = true) {
        EditSetVariationSelector(
            state = state,
            showVariationDialog = {}
        )
    }
}
