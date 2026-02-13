package com.lift.bro.presentation.wrapped

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.wrapped.usecase.GetMostConsistentDayUseCase
import com.lift.bro.presentation.wrapped.usecase.GetMostConsistentMonthUseCase
import com.lift.bro.presentation.wrapped.usecase.GetMostConsistentVariationUseCase
import com.lift.bro.presentation.wrapped.usecase.GetVariationConsistencyUseCase
import com.lift.bro.ui.Space
import com.lift.bro.ui.calendar.today
import com.lift.bro.ui.dialog.InfoSpeechBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.fullName
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.wrapped_consistency_header_title
import lift_bro.core.generated.resources.wrapped_consistency_screen_most_consistent_text
import lift_bro.core.generated.resources.wrapped_consistency_speech_bubble_title
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.padding.vertical.padding
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.rememberInteractor

@Serializable
@Immutable
data class WrappedConsistencyState(
    val dates: Set<LocalDate>,
    val mostConsistentMonth: Pair<Month, Int>,
    val mostConsistentDay: Pair<DayOfWeek, Int>,
    val mostConsistentLift: Pair<String, Int>,
)

@Composable
fun rememberWrappedConsistencyInteractor(
    year: Int = LocalWrappedYear.current,
    getVariationConsistencyUseCase: GetVariationConsistencyUseCase = GetVariationConsistencyUseCase(),
    getMostConsistentMonthUseCase: GetMostConsistentMonthUseCase = GetMostConsistentMonthUseCase(),
    getMostConsistentDayUseCase: GetMostConsistentDayUseCase = GetMostConsistentDayUseCase(),
    getMostConsistentVariationUseCase: GetMostConsistentVariationUseCase = GetMostConsistentVariationUseCase(),
) = rememberInteractor<WrappedConsistencyState?, Nothing>(
    initialState = null,
    source = {
        val startDate = LocalDate(year, 1, 1)
        val endDate = LocalDate(year, 12, 31)
        combine(
            getVariationConsistencyUseCase(startDate = startDate, endDate = endDate),
            getMostConsistentMonthUseCase(startDate = startDate, endDate = endDate),
            getMostConsistentDayUseCase(startDate = startDate, endDate = endDate),
            getMostConsistentVariationUseCase(startDate = startDate, endDate = endDate),
        ) { dates, month, day, variation ->
            WrappedConsistencyState(
                dates = dates.keys,
                mostConsistentMonth = month,
                mostConsistentDay = day,
                mostConsistentLift = variation.first.fullName to variation.second,
            )
        }
    }
)

@Composable
fun WrappedConsistencyScreen(
    interactor: Interactor<WrappedConsistencyState?, Nothing> = rememberWrappedConsistencyInteractor(),
) {
    val state by interactor.state.collectAsState()

    state?.let {
        WrappedConsistencyScreen(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedConsistencyScreen(
    state: WrappedConsistencyState,
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
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
                text = stringResource(Res.string.wrapped_consistency_header_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        val dates = state.dates.groupBy { it.month }

        item(
            span = { GridItemSpan(2) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                if (dates.isNotEmpty()) {
                    InfoSpeechBubble(
                        title = {
                            Text(
                                stringResource(Res.string.wrapped_consistency_speech_bubble_title),
                                style = MaterialTheme.typography.displaySmall
                            )
                        },
                        message = {
                            Text(
                                text = stringResource(Res.string.wrapped_consistency_screen_most_consistent_text),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Space(MaterialTheme.spacing.half)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    with(state.mostConsistentMonth) {
                                        Text(
                                            "Month",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            // year and day do not matter
                                            LocalDate(2025, first, 1)
                                                .toString("MMMM"),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            "${second}x",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.displaySmall,
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    with(state.mostConsistentDay) {
                                        Text(
                                            "Day",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            first.name,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            "${second}x",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.displaySmall,
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    with(state.mostConsistentLift) {
                                        Text(
                                            "Lift",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            first,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            "${second}x",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.displaySmall,
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        items(Month.entries.toList()) { month ->
            ConsistencyMonthItem(
                year = 2025,
                month = month,
                dates = dates[month] ?: emptyList(),
            )
        }
    }
}

@Composable
private fun ConsistencyMonthItem(
    modifier: Modifier = Modifier,
    year: Int,
    month: Month,
    dates: List<LocalDate>,
) {
    val days = dates.map { it.dayOfMonth }.toSet()
    Column(
        modifier = modifier,
    ) {
        Text(month.name)
        var day = LocalDate(year, month, 1)
        Row {
            DayOfWeek.entries.toList().forEach {
                Text(
                    modifier = Modifier.weight(1f),
                    text = it.name.first().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        Space(MaterialTheme.spacing.half)
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.eighth)
        ) {
            repeat((0..6).count()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.eighth)
                ) {
                    DayOfWeek.entries.toList().forEach { dayOfWeek ->
                        if (dayOfWeek == day.dayOfWeek && month == day.month) {
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f)
                                    .border(
                                        width = 1.dp,
                                        shape = MaterialTheme.shapes.small,
                                        color = if (days.contains(
                                                day.dayOfMonth
                                            )
                                        ) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.onBackground
                                        }
                                    )
                                    .background(
                                        color = if (days.contains(
                                                day.dayOfMonth
                                            )
                                        ) {
                                            MaterialTheme.colorScheme.tertiary
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = MaterialTheme.shapes.small,
                                    ),
                            )
                            day = day.plus(DatePeriod(days = 1))
                        } else {
                            Box(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun WrappedConsistencyScreenPreview(@PreviewParameter(DarkModeProvider::class) dark: Boolean) {
    PreviewAppTheme(isDarkMode = dark) {
        WrappedConsistencyScreen(
            state = WrappedConsistencyState(
                dates = setOf(
                    today,
                    LocalDate(today.year, 1, 1),
                ),
                mostConsistentMonth = Month.DECEMBER to 10,
                mostConsistentDay = DayOfWeek.MONDAY to 18,
                mostConsistentLift = "Squat" to 10,
            )
        )
    }
}
