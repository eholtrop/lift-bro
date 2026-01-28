package com.lift.bro.presentation.wrapped.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Goal
import tv.dpal.flowvi.Interactor
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoSpeechBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.compose.horizontal_padding.padding
import com.lift.bro.compose.vertical_padding.padding
import kotlinx.coroutines.delay
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.wrapped_goals_add_button_content_description
import lift_bro.core.generated.resources.wrapped_goals_delete_button_content_description
import lift_bro.core.generated.resources.wrapped_goals_header_title
import lift_bro.core.generated.resources.wrapped_goals_section_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WrappedGoalsScreen(
    interactor: Interactor<WrappedGoalsState, WrappedGoalsEvent>,
) {
    val state by interactor.state.collectAsState()

    WrappedGoalsScreen(
        state = state,
        goalAdded = { interactor(WrappedGoalsEvent.GoalAdded(Goal(name = ""))) },
        goalChanged = { goal, name -> interactor(WrappedGoalsEvent.GoalNameChanged(goal, name)) },
        goalRemoved = { interactor(WrappedGoalsEvent.GoalRemoved(it)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedGoalsScreen(
    state: WrappedGoalsState,
    goalAdded: () -> Unit = {},
    goalChanged: (Goal, String) -> Unit = { _, _ -> },
    goalRemoved: (Goal) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
    ) {
        stickyHeader {
            Text(
                modifier = Modifier
                    .background(color = BottomSheetDefaults.ContainerColor)
                    .padding(
                        horizontal = MaterialTheme.spacing.one,
                        top = MaterialTheme.spacing.oneAndHalf,
                        bottom = MaterialTheme.spacing.threeQuarters
                    ),
                text = stringResource(Res.string.wrapped_goals_header_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            InfoSpeechBubble(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        "Some inspiration!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                message = {
                    var goal by remember { mutableStateOf(options.random()) }
                    var visibility by remember { mutableStateOf(true) }

                    Column(
                        modifier = Modifier.defaultMinSize(minHeight = 52.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        AnimatedVisibility(
                            visible = visibility,
                            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                            exit = fadeOut(animationSpec = tween(durationMillis = 1000))
                        ) {
                            Text(
                                text = goal,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }

                    LaunchedEffect(Unit) {
                        while (true) {
                            visibility = false
                            delay(1100)
                            goal = options.random()
                            visibility = true
                            delay(5000)
                        }
                    }
                }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        role = Role.Button,
                        onClick = goalAdded
                    )
                    .padding(
                        start = MaterialTheme.spacing.one,
                        end = MaterialTheme.spacing.half,
                        vertical = MaterialTheme.spacing.half,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.wrapped_goals_section_title),
                    style = MaterialTheme.typography.titleLarge
                )

                Space(MaterialTheme.spacing.half)

                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.wrapped_goals_add_button_content_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        itemsIndexed(
            items = state.goals,
            key = { _, goal -> goal.id }
        ) { index, goal ->
            GoalCard(
                modifier = Modifier.animateItem(),
                goal = goal.name,
                onGoalChanged = {
                    goalChanged(goal, it)
                },
                onDelete = {
                    goalRemoved(goal)
                }
            )
        }
    }
}

private val options = listOf(
    "Exceed my Bench Press max by 5%",
    "Be ready for beach season",
    "Get off the couch without grunting",
    "Do a full chin up",
    "Do a push-up without using my knees",
    "Do 100 push-ups a day",
    "Stretch 3 days a week",
    "Get Swole",
    "Go to the Gym 8 times a week",
    "Beat \"The Rock\" in an arm wrestling match"
)

@Composable
private fun GoalCard(
    modifier: Modifier = Modifier,
    goal: String,
    onGoalChanged: (String) -> Unit,
    onDelete: () -> Unit,
    focusRequester: FocusRequester = FocusRequester(),
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var name by remember { mutableStateOf(goal) }

        TextField(
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            value = name,
            textStyle = MaterialTheme.typography.bodyMedium,
            onValueChange = {
                name = it
                onGoalChanged(it)
            },
            placeholder = {
                Text(
                    text = options.random(),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            colors = TextFieldDefaults.transparentColors(),
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(Res.string.wrapped_goals_delete_button_content_description)
            )
        }
    }
}

@Preview
@Composable
fun WrappedGoalsScreenPreview(@PreviewParameter(DarkModeProvider::class) dark: Boolean) {
    PreviewAppTheme(
        isDarkMode = dark
    ) {
        WrappedGoalsScreen(
            state = WrappedGoalsState(
                goals = listOf(
                    Goal(name = "Do a push-up without using me knees"),
                    Goal(name = "Exceed my Bench Press max by 5%"),
                    Goal(name = "Be ready for beach season"),
                    Goal(name = "Get off the couch without grunting"),
                )
            )
        )
    }
}
