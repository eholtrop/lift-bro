package com.lift.bro.preview

import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider


class DarkModeProvider: PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(
            true, false
        )
}