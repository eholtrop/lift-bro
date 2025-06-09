package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.presentation.set.EditSetScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun EditSetScreenPreview() {
    AppTheme {
        EditSetScreen(
            setId = null,
            variationId = null,
            liftId = null,
            setSaved = {},
            createLiftClicked = {}
        )
    }
}