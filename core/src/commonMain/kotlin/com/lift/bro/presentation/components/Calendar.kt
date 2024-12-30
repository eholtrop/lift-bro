package com.lift.bro.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.ui.Space
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.math.min


object ComposeCalendarDefaults {

    fun singleDateSelectionShapes() = ComposeCalendarShapes(
        startDateForegroundShape = RoundedCornerShape(percent = 50),
        endDateForegroundShape = RoundedCornerShape(percent = 50),
        startDateBackgroundShape = RoundedCornerShape(percent = 50),
        endDateBackgroundShape = RoundedCornerShape(percent = 50),
        selectionDateShape = RoundedCornerShape(percent = 50),
        noSelectionShape = RoundedCornerShape(percent = 50),
    )

    fun rangeDateSelectionShapes() = ComposeCalendarShapes(
        startDateForegroundShape = RoundedCornerShape(percent = 50),
        endDateForegroundShape = RoundedCornerShape(percent = 50),
        startDateBackgroundShape = RoundedCornerShape(
            topStart = 50f,
            topEnd = 0f,
            bottomStart = 50f,
            bottomEnd = 0f

        ),
        endDateBackgroundShape = RoundedCornerShape(
            topStart = 0f,
            topEnd = 50f,
            bottomStart = 0f,
            bottomEnd = 50f
        ),
        selectionDateShape = RoundedCornerShape(percent = 0),
        noSelectionShape = RoundedCornerShape(percent = 50),
    )
}

data class ComposeCalendarShapes(
    val startDateForegroundShape: Shape,
    val endDateForegroundShape: Shape,
    val startDateBackgroundShape: Shape,
    val endDateBackgroundShape: Shape,
    val selectionDateShape: Shape,
    val noSelectionShape: Shape,
)

private const val CALENDAR_MAX_MONTH_SIZE = 48
private const val CALENDAR_INITIAL_PAGE = CALENDAR_MAX_MONTH_SIZE / 2


/**
 * A Jetpack Compose Calendar Implementation
 */
@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    contentPadding: PaddingValues,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    dotsForDate: (LocalDate) -> List<Color>,
    dateSelected: (LocalDate) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        val pagerState = rememberPagerState(
            initialPage = CALENDAR_INITIAL_PAGE,
            pageCount = { CALENDAR_MAX_MONTH_SIZE }
        )

        CalendarContent(
            pagerState = pagerState,
            selection = selectedDate,
            dateSelected = dateSelected,
            horizontalArrangement = horizontalArrangement,
            verticalArrangement = verticalArrangement,
            dotsForDate = dotsForDate,
            contentPadding = contentPadding
        )
    }
}

val today get() = Clock.System.todayIn(TimeZone.currentSystemDefault())

@Composable
private fun CalendarContent(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    selection: LocalDate?,
    contentPadding: PaddingValues,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    dateSelected: (LocalDate) -> Unit,
    dotsForDate: (LocalDate) -> List<Color>
) {

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.semantics { heading() }.padding(
                    start = MaterialTheme.spacing.one
                ),
                text = pagerState.currentMonth.toString("MMMM - yyyy"),
                style = MaterialTheme.typography.titleMedium
            )

            Space()

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Space(MaterialTheme.spacing.half)

        Row(
            modifier = Modifier.padding(
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
            )
        ) {
            DayOfWeek.values().forEach {
                Text(
                    modifier = Modifier.weight(1f),
                    text = it.toString().take(3).lowercase(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Space(MaterialTheme.spacing.half)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            pageSpacing = MaterialTheme.spacing.half,
            beyondViewportPageCount = 1
        ) { page ->
            val currentMonth = today.plus(DatePeriod(months = CALENDAR_INITIAL_PAGE - page))
            val startDate = currentMonth.minus(DatePeriod(days = today.dayOfMonth - 1))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = verticalArrangement
            ) {

                for (weekOffset in 0..5) {
                    val currentWeek =
                        startDate.minus(DatePeriod(days = startDate.dayOfWeek.ordinal))
                            .plus(DatePeriod(days = 7 * weekOffset))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = horizontalArrangement
                    ) {
                        for (dayOffset in 0..6) {
                            val currentDay = currentWeek.plus(DatePeriod(days = dayOffset))

                            val dots by remember(currentDay) {
                                mutableStateOf(dotsForDate(currentDay))
                            }

                            CalendarDate(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                date = currentDay,
                                style = when {
                                    selection == currentDay -> CalendarDateStyle.Selected
                                    currentDay.month == currentMonth.month -> CalendarDateStyle.Enabled
                                    else -> CalendarDateStyle.Disabled
                                },
                                dots = dots,
                                onClick = dateSelected,
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class CalendarDateStyle {
    Enabled, Disabled, Selected;
}

@Composable
private fun CalendarDate(
    modifier: Modifier = Modifier,
    date: LocalDate,
    style: CalendarDateStyle,
    onClick: (LocalDate) -> Unit,
    dots: List<Color> = emptyList(),
) {

    val backgroundColor = when (style) {
        CalendarDateStyle.Selected -> MaterialTheme.colorScheme.secondary
        CalendarDateStyle.Enabled -> MaterialTheme.colorScheme.surface
        CalendarDateStyle.Disabled -> MaterialTheme.colorScheme.surfaceDim
    }

    val contentColor = when (style) {
        CalendarDateStyle.Selected -> MaterialTheme.colorScheme.secondaryContainer
        CalendarDateStyle.Enabled -> MaterialTheme.colorScheme.onSurface
        CalendarDateStyle.Disabled -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small,
            )
            .clickable {
                onClick(date)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium
        )

        Space(MaterialTheme.spacing.quarter)

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
        ) {
            dots.forEach {
                Box(
                    modifier = Modifier.background(
                        color = if (style == CalendarDateStyle.Selected) contentColor else it,
                        shape = CircleShape,
                    ).size(4.dp)
                )
            }
        }
    }
}

private val PagerState.currentMonth get() = today.plus(DatePeriod(months = CALENDAR_INITIAL_PAGE - currentPage))
