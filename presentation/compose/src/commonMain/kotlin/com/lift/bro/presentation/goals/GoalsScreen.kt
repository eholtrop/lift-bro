package com.lift.bro.presentation.goals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lift.bro.domain.models.Goal
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun GoalsScreen(
    interactor: GoalsInteractor = rememberGoalsInteractor(),
) {
    val state by interactor.state.collectAsState()

    GoalsScreen(
        state = state,
        onEvent = {
            interactor(it)
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GoalsScreen(
    state: GoalsState,
    onEvent: (GoalsEvents) -> Unit = {},
) {
    LiftingScaffold(
        title = {
            Text("2026 Goals")
        },
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth().animateContentSize(),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            stickyHeader {
                Button(
                    onClick = {
                        onEvent(GoalsEvents.AddGoalClicked)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Goals",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Space(MaterialTheme.spacing.half)
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = ""
                        )
                    }
                }
            }

            items(
                items = state.goals,
                key = { it.id }
            ) { item ->

                val animatedBackgroundColor by animateColorAsState(
                    targetValue = if (item.achieved) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )

                Row(
                    modifier = Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                animatedBackgroundColor,
                                Color.Transparent,
                            )
                        ),
                        shape = MaterialTheme.shapes.large,
                    ).padding(
                        horizontal = MaterialTheme.spacing.one,
                        vertical = MaterialTheme.spacing.half
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            onEvent(GoalsEvents.ToggleGoalAchieved(item.id))
                        }
                    ) {
                        if (item.achieved) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Achieved"
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = ""
                            )
                        }
                    }
                    var name by remember { mutableStateOf(item.name) }
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = name,
                        colors = TextFieldDefaults.transparentColors(),
                        onValueChange = {
                            name = it
                            onEvent(GoalsEvents.GoalChanged(item.id, it))
                        },
                        textStyle = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = {
                            onEvent(GoalsEvents.DeleteGoalClicked(item.id))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun GoalsScreenPreview(@PreviewParameter(DarkModeProvider::class) mode: Boolean) {
    PreviewAppTheme(
        isDarkMode = mode
    ) {
        GoalsScreen(
            state = GoalsState(
                goals = listOf(
                    Goal(
                        name = "130 in Bench Press",
                        achieved = true
                    ),
                    Goal(
                        name = "310 in Deadlift",
                        achieved = false
                    ),
                    Goal(
                        name = "650 SBD",
                        achieved = true
                    ),
                    Goal(
                        name = "",
                    ),
                )
            )
        )
    }
}
