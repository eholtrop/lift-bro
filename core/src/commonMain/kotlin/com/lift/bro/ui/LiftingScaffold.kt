package com.lift.bro.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LiftingScaffold(
    fabText: String? = null,
    fabClicked: () -> Unit,
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
        floatingActionButton = {
            fabText?.let {
                Button(
                    onClick = fabClicked
                ) {
                    Text(fabText)
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) {
        content(it)
    }
}