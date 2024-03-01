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

            LaunchedEffect("debug_mode") {
//                if (BuildConfig.DEBUG) {
//                    BackupRestore.restore(
//                        Backup(
//                            listOf(
//                                Lift(
//                                    id = "1",
//                                    name = "Squat"
//                                ),
//                                Lift(
//                                    id = "2",
//                                    name = "Press"
//                                )
//                            ),
//                            listOf(
//                                Variation(
//                                    id = "back squat",
//                                    liftId = "1",
//                                    name = "Back"
//                                ),
//                                Variation(
//                                    id = "front squat",
//                                    liftId = "1",
//                                    name = "Front"
//                                ),
//                                Variation(
//                                    id = "military press",
//                                    liftId = "2",
//                                    name = "Military"
//                                ),
//                                Variation(
//                                    id = "bench press",
//                                    liftId = "2",
//                                    name = "Bench"
//                                ),
//                            ),
//                            listOf(
//                                LBSet(
//                                    id = "1",
//                                    variationId = "back squat",
//                                    weight = 170.0,
//                                    reps = 1,
//                                    tempoDown = 3,
//                                    tempoHold = 1,
//                                    tempoUp = 1,
//                                ),
//                                LBSet(
//                                    id = "1",
//                                    variationId = "bench press",
//                                    weight = 150.0,
//                                    reps = 1,
//                                    tempoDown = 3,
//                                    tempoHold = 1,
//                                    tempoUp = 1,
//                                ),
//                                LBSet(
//                                    id = "1",
//                                    variationId = "back squat",
//                                    weight = 190.0,
//                                    reps = 1,
//                                    tempoDown = 3,
//                                    tempoHold = 1,
//                                    tempoUp = 1,
//                                ),
//                            ),
//                        )
//                    ).collect()
//                }
            }
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
