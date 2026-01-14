package com.lift.bro.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.AnimatedText
import com.lift.bro.ui.AnimatedTextDefaults
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.toString
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

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

@Composable
fun rememberCalendarState() = rememberPagerState(
    initialPage = CALENDAR_INITIAL_PAGE,
    pageCount = { CALENDAR_MAX_MONTH_SIZE }
)

/**
 * A Jetpack Compose Calendar Implementation
 */
@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    contentPadding: PaddingValues,
    pagerState: PagerState = rememberCalendarState(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    dateDecorations: @Composable (LocalDate, @Composable () -> Unit) -> Unit,
    dateSelected: (LocalDate) -> Unit,
    contentForMonth: @Composable (year: Int, month: Month) -> Unit = { year, month ->
        CalendarMonth(
            year = year,
            month = month,
            selection = selectedDate,
            dateSelected = dateSelected,
            dateDecorations = dateDecorations,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
        )
    },
) {
    Column(
        modifier = modifier,
    ) {
        CalendarContent(
            pagerState = pagerState,
            selection = selectedDate,
            dateSelected = dateSelected,
            contentPadding = contentPadding,
            contentForMonth = contentForMonth
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
    dateSelected: (LocalDate) -> Unit,
    contentForMonth: @Composable (year: Int, month: Month) -> Unit,
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
            Column(
                modifier = Modifier.semantics { heading() }
            ) {
                AnimatedText(
                    text = pagerState.currentMonth.toString("MMMM"),
                    style = MaterialTheme.typography.titleMedium,
                )

                AnimatedText(
                    text = pagerState.currentMonth.toString("yyyy"),
                    style = MaterialTheme.typography.bodyMedium,
                    transitionForChar = { char, index ->
                        if (char.isDigit()) {
                            AnimatedTextDefaults.slideInOut(this, char, index)
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    }
                )
            }

            Space()

            AnimatedVisibility(
                visible = selection != today,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    onClick = {
                        dateSelected(today)
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(CALENDAR_INITIAL_PAGE)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Today",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
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
            val currentMonth = today.plus(DatePeriod(months = page - CALENDAR_INITIAL_PAGE))
            contentForMonth(currentMonth.year, currentMonth.month)
        }
    }
}

@Preview
@Composable
fun CalendarPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean,
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        val pagerState = rememberCalendarState()

        Calendar(
            selectedDate = LocalDate(2024, 1, 15),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            pagerState = pagerState,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
            dateDecorations = { date, content ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                    // Show workout indicator dots on certain days
                    if (date.dayOfMonth % 3 == 0 && date.month == pagerState.currentMonth.month) {
                        Canvas(
                            modifier = Modifier.size(4.dp)
                        ) {
                            drawCircle(color = Color(0xFF2196F3))
                        }
                    }
                }
            },
            dateSelected = {}
        )
    }
}

val PagerState.currentMonth get() = today.plus(DatePeriod(months = currentPage - CALENDAR_INITIAL_PAGE))
