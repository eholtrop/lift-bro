package com.lift.bro.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.onboarding_consent_screen_consent
import lift_bro.core.generated.resources.privacy_policy
import lift_bro.core.generated.resources.terms_and_conditions
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConsentCheckBoxField(
    modifier: Modifier = Modifier,
    accepted: Boolean,
    acceptanceChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
    ) {
        Checkbox(
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
                    LinkAnnotation.Url("https://app.termly.io/policy-viewer/policy.html?policyUUID=bc17a69a-5f52-4235-b717-7c039cb7adef"),
                    start = consent.indexOf(tnc),
                    end = consent.indexOf(tnc) + tnc.length,
                )
                addStyle(
                    style = SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold),
                    start = consent.indexOf(tnc),
                    end = consent.indexOf(tnc) + tnc.length,
                )

                addLink(
                    LinkAnnotation.Url("https://app.termly.io/policy-viewer/policy.html?policyUUID=f1327329-7d60-4fe4-ad33-1550505d6897"),
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