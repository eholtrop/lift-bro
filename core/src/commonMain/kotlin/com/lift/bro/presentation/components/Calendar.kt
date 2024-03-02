package com.lift.bro.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    contentPadding: PaddingValues,
    dateSelected: (LocalDate) -> Unit,
    date: @Composable RowScope.(LocalDate) -> Unit
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
            date = date,
            contentPadding = contentPadding
        )
    }
}

val today get() = Clock.System.todayIn(TimeZone.currentSystemDefault())

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun CalendarContent(
    pagerState: PagerState,
    selection: LocalDate?,
    contentPadding: PaddingValues,
    dateSelected: (LocalDate) -> Unit,
    date: @Composable RowScope.(LocalDate) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.padding(
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous Month",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Space()
        Text(
            text = pagerState.currentMonth.month.toString(),
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
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next Month",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }

    Row(
        modifier = Modifier.padding(
            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current)
        )
    ) {
        DayOfWeek.values().forEach {
            Text(
                modifier = Modifier.weight(1f),
                text = it.toString().take(1),
                textAlign = TextAlign.Center,
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = contentPadding,
    ) { page ->
        val monthOffset = remember { page - pagerState.initialPage }
        val currentMonth = today.plus(DatePeriod(months = monthOffset))
        val startDate = currentMonth.minus(DatePeriod(days = today.dayOfMonth - 1))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            for (weekOffset in 0..5) {
                val currentWeek = startDate.minus(DatePeriod(days = startDate.dayOfWeek.ordinal))
                    .plus(DatePeriod(days = 7 * weekOffset))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    for (dayOffset in 0..6) {
                        val currentDay = currentWeek.plus(DatePeriod(days = dayOffset))

                        if (currentDay.month == currentMonth.month) {

                            val selected = currentDay == selection

                            date(currentDay)
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private val PagerState.currentMonth get() = today.plus(DatePeriod(months = this.currentPage - this.initialPage))
