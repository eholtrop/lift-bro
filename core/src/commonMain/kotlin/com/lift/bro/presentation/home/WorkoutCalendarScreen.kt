package com.lift.bro.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Excercise
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.presentation.ads.AdBanner
import com.lift.bro.presentation.excercise.SetInfoRow
import com.lift.bro.ui.Calendar
import com.lift.bro.ui.Space
import com.lift.bro.ui.saver.MutableLocalDateSaver
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toString
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.workout_calendar_edit_daily_notes_cta
import org.jetbrains.compose.resources.stringResource

@Composable
fun WorkoutCalendarScreen(
    modifier: Modifier = Modifier,
    variationClicked: (Variation, LocalDate) -> Unit,
    excercises: List<Excercise>,
    logs: List<LiftingLog>,
) {

    var selectedDate by rememberSaveable(saver = MutableLocalDateSaver) { mutableStateOf(today) }

    val setDateMap = excercises.groupBy { it.date }

    val selectedDateSets: List<Excercise> = setDateMap[selectedDate] ?: emptyList()

    val dailyLogs = logs.associateBy { it.date }

    val subscriptionType by LocalSubscriptionStatusProvider.current

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
    ) {

        item {
            Calendar(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight(),
                selectedDate = selectedDate,
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
                dateSelected = {
                    selectedDate = it
                },
                dateDecorations = { date, day ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dailyLogs[date] != null) {
                            Icon(
                                modifier = Modifier
                                    .padding(
                                        top = MaterialTheme.spacing.quarter,
                                        start = MaterialTheme.spacing.quarter,
                                    )
                                    .size(8.dp).align(Alignment.TopStart),
                                imageVector = Icons.Default.Edit,
                                tint = if (date == selectedDate) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            day()
                            Space(MaterialTheme.spacing.quarter)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                            ) {
                                setDateMap[date]?.map {
                                    it.variation.lift?.color?.toColor()
                                        ?: MaterialTheme.colorScheme.primary
                                }?.take(4)?.forEachIndexed { index, color ->
                                    Box(
                                        modifier = Modifier.background(
                                            color = if (date == selectedDate) MaterialTheme.colorScheme.onPrimary else color,
                                            shape = CircleShape,
                                        ).size(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }


        if (subscriptionType == SubscriptionType.None) {
            item {
                AdBanner(modifier = Modifier.defaultMinSize(minHeight = 52.dp).fillMaxWidth())
            }
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(height = 2.dp)
                    .background(MaterialTheme.colorScheme.surfaceDim)
            ) {}
        }

        item {
            Column {
                var showNotesDialog by remember { mutableStateOf(false) }
                var todaysNotes by remember(selectedDate) {
                    mutableStateOf(
                        dailyLogs[selectedDate]?.notes ?: ""
                    )
                }

                if (showNotesDialog) {
                    AlertDialog(
                        onDismissRequest = { showNotesDialog = false },
                        confirmButton = {
                            Button(
                                onClick = {
                                    GlobalScope.launch {
                                        dependencies.database.logDataSource.save(
                                            id = dailyLogs[selectedDate]?.id ?: uuid4().toString(),
                                            date = dailyLogs[selectedDate]?.date ?: selectedDate,
                                            notes = todaysNotes,
                                            vibe_check = dailyLogs[selectedDate]?.vibe?.toLong()
                                        )
                                        showNotesDialog = false
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showNotesDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        },
                        title = {
                            Text("Notes for:\n${selectedDate.toString("EEEE, MMM d - yyyy")}")
                        },
                        text = {
                            val focusRequester = FocusRequester()
                            TextField(
                                modifier = Modifier.defaultMinSize(minHeight = 128.dp)
                                    .focusRequester(focusRequester),
                                value = todaysNotes,
                                onValueChange = { todaysNotes = it },
                                placeholder = {
                                    Text("Goals for today?\nFeeling especially spicy?\nSoreness or pain?")
                                }
                            )

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedDate.toString("EEEE, MMM d - yyyy"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(
                        onClick = {
                            showNotesDialog = true
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.workout_calendar_edit_daily_notes_cta)
                        )
                    }
                }

                dailyLogs[selectedDate]?.let {
                    Text(
                        text = it.notes,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        items(
            selectedDateSets
        ) { excercise ->
            Card(
                modifier = Modifier.animateItem(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                val variation = excercise.variation

                Row(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.one,
                        vertical = MaterialTheme.spacing.half
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                            .clickable(
                                role = Role.Button,
                                onClick = { variationClicked(variation, selectedDate) }
                            )
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle()
                                ) {
                                    append("${variation.name} ${variation.lift?.name}")
                                }

                                val mer = excercise.sets.sumOf { it.mer }
                                if (mer > 0 && LocalShowMERCalcs.current?.enabled == true) {
                                    withStyle(
                                        style = MaterialTheme.typography.labelSmall.toSpanStyle()
                                    ) {
                                        append(" ")
                                        append("(+${mer}mer)")
                                    }
                                }
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )

                        excercise.sets.sortedByDescending { it.weight }
                            .forEach { set ->
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                        .defaultMinSize(minHeight = 44.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    SetInfoRow(set = set)
                                }
                            }
                    }

                    Space(MaterialTheme.spacing.half)

                    Box(
                        modifier = Modifier.background(
                            color = variation.lift?.color?.toColor()
                                ?: MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ).height(MaterialTheme.spacing.oneAndHalf).aspectRatio(1f),
                        content = {}
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}