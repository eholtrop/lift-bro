package com.lift.bro.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.onboarding_consent_screen_consent
import lift_bro.core.generated.resources.onboarding_consent_screen_consent_checkbox_content_description
import lift_bro.core.generated.resources.privacy_policy
import lift_bro.core.generated.resources.terms_and_conditions
import lift_bro.core.generated.resources.url_privacy_policy
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConsentCheckBoxField(
    modifier: Modifier = Modifier,
    accepted: Boolean,
    acceptanceChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {}
    ) {
        val cd = stringResource(Res.string.onboarding_consent_screen_consent_checkbox_content_description)
        Checkbox(
            modifier = Modifier.semantics {
                contentDescription = cd
            },
            checked = accepted, onCheckedChange = acceptanceChanged
        )
        Space(MaterialTheme.spacing.one)
        Text(
            text = buildAnnotatedString {
                val tnc = stringResource(Res.string.terms_and_conditions)
                val privacy = stringResource(Res.string.privacy_policy)
                val consent = stringResource(
                    Res.string.onboarding_consent_screen_consent,
                    tnc,
                    privacy
                )

                append(consent)

                addLink(
                    LinkAnnotation.Url(stringResource(Res.string.url_privacy_policy)),
                    start = consent.indexOf(tnc),
                    end = consent.indexOf(tnc) + tnc.length,
                )
                addStyle(
                    style = SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold),
                    start = consent.indexOf(tnc),
                    end = consent.indexOf(tnc) + tnc.length,
                )

                addLink(
                    LinkAnnotation.Url(stringResource(Res.string.url_privacy_policy)),
                    start = consent.indexOf(privacy),
                    end = consent.indexOf(privacy) + privacy.length
                )
                addStyle(
                    style = SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold),
                    start = consent.indexOf(privacy),
                    end = consent.indexOf(privacy) + privacy.length
                )
            }
        )
    }
}