package com.lift.bro.presentation.wrapped

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

private val options = listOf(
    "Exceed my Bench Press max by 5%",
    "Be ready for beach season",
    "Get off the couch without grunting",
    "Make the ex Jealous ;)",
    "Do a full chin up",
    "Do a push-up without using me knees",
    "Do 100 push-ups a month",
    "Stretch 3 days a week",
    "Get Swole"
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedGoalsScreen(
    goals: List<String> = emptyList(),
) {
    LiftingScaffold(
        title = {
            Text("Now... How about Goals for next year?")
        }
    ) { padding ->

        val goals = remember { mutableStateListOf(*goals.toTypedArray()) }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {

            item {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                goals.add(0, "")
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Goals",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Space(MaterialTheme.spacing.half)

                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Goal",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
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

            itemsIndexed(goals) { index, goal ->
                GoalCard(
                    goal = goal,
                    onGoalChanged = {
                        goals[index] = it
                    },
                    onDelete = {
                        goals.removeAt(index)
                    }
                )
            }
        }
    }
}


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
            onValueChange = {
                name = it
                onGoalChanged(it)
            },
            placeholder = { Text(options.random()) },
                        colors = TextFieldDefaults.transparentColors()
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove Goal"
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
            goals = listOf(
                "Get Swole",
                "super long answer that will end up being multiple lines because I have to test this WEEEEEEEEEEEE",
                "",
                "Exceed my Bench Press max by 5%",
            )
        )
    }
}
