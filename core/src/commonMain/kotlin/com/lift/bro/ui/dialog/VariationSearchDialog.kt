package com.lift.bro.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.maxText
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.weightFormat
import com.lift.bro.utils.toString

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VariationSearchDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    variationSelected: (Variation) -> Unit,
) {
    var show by remember { mutableStateOf(visible) }
    var query by remember { mutableStateOf("") }

    var variations by remember { mutableStateOf(emptyList<Variation>()) }


    LaunchedEffect(query) {
        variations = dependencies.database.variantDataSource.getAll()
            .filter { it.fullName.contains(query, ignoreCase = true) }
    }

    Box {
        AnimatedVisibility(
            visible = show,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable(
                        onClick = {
                            onDismissRequest()
                        }
                    )
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .6f))
            )
        }


        Column {
            AnimatedVisibility(
                visible = show,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
            ) {
                val focusRequester = FocusRequester()
                TextField(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .focusRequester(focusRequester),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onDismissRequest()
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    placeholder = { Text("Add Exercise") },
                    value = query,
                    onValueChange = { query = it },
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }

            Space(MaterialTheme.spacing.one)

            AnimatedVisibility(
                modifier = Modifier.weight(1f),
                visible = show,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                VariationSearchContent(
                    modifier = Modifier.fillMaxWidth(),
                    variations = variations,
                    variationSelected = variationSelected
                )
            }
        }
    }

    BackHandler(visible) {
        onDismissRequest()
    }

    LaunchedEffect(visible) {
        show = visible
    }
}

@Composable
private fun VariationSearchContent(
    modifier: Modifier = Modifier,
    variations: List<Variation> = emptyList(),
    variationSelected: (Variation) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        ),
    ) {
        LazyColumn(
            modifier = Modifier.animateContentSize().navigationBarsPadding(),
            contentPadding = PaddingValues(
                vertical = MaterialTheme.spacing.one,
                horizontal = MaterialTheme.spacing.half
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            items(
                variations.sortedBy { it.name }
                    .sortedByDescending { it.favourite },
                key = { it.id }) { variation ->
                Column(
                    modifier = Modifier.animateItem()
                        .fillMaxWidth()
                        .clickable(
                            onClick = { variationSelected(variation) }
                        )
                        .minimumInteractiveComponentSize()
                        .padding(
                            horizontal = MaterialTheme.spacing.half,
                        ),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            variation.fullName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (variation.favourite) {
                            Space(MaterialTheme.spacing.half)
                            Icon(
                                modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp),
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favourite"
                            )
                        }
                    }
                    Text(
                        variation.maxText(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val latestSet = dependencies.database.setDataSource.getAll(variation.id)
                        .maxByOrNull { it.date }

                    if (latestSet != null) {
                        Text(
                            text = "${latestSet.date.toString("MMM d")}: ${weightFormat(latestSet.weight)} x ${latestSet.reps}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        latestSet.tempo.render()
                        if (latestSet.notes.isNotBlank()) {
                            Text(
                                latestSet.notes,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}