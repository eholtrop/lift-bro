package com.lift.bro.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.lift.bro.config.BuildConfig
import com.lift.bro.data.Backup
import com.lift.bro.data.BackupRestore
import com.lift.bro.debugBackup
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import kotlinx.coroutines.flow.collect


@Composable
fun App(
    coordinator: Coordinator = dependencies.coordinator,
) {
    AppTheme() {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            coordinator.render()

//            LaunchedEffect("debug_mode") {
//                if (BuildConfig.DEBUG) {
//                    BackupRestore.restore(
//                        backup = debugBackup
//                    ).collect()
//                }
//            }
        }
    }
}

object Spacing {
    val two = 32.dp
    val oneAndHalf = 24.dp
    val one = 16.dp
    val half = 8.dp
    val quarter = 4.dp
}

object LiftingTheme {
    val spacing: Spacing = Spacing
}

val MaterialTheme.spacing get() = LiftingTheme.spacing
