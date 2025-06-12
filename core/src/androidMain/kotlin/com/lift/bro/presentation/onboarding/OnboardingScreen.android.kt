package com.lift.bro.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

@Composable
actual fun Modifier.resourceId(id: String): Modifier {
    return this.semantics(mergeDescendants = true) {
        testTagsAsResourceId = true
    }.testTag(id)
}