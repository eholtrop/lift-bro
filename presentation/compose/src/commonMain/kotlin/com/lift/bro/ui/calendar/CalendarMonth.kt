package com.lift.bro.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun CalendarMonth(
    modifier: Modifier = Modifier,
    year: Int,
    month: Month,
    selection: LocalDate?,
    dateSelected: (LocalDate) -> Unit,
    dateDecorations: @Composable (LocalDate, @Composable () -> Unit) -> Unit,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
) {
    val startDate by remember(year, month) { mutableStateOf(LocalDate(year, month, 1)) }

    Column(
        modifier = modifier,
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

                    CalendarDate(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        date = currentDay,
                        style = when {
                            selection == currentDay -> CalendarDateStyle.Selected
                            today == currentDay -> CalendarDateStyle.Today
                            currentDay.month == month -> CalendarDateStyle.Enabled
                            else -> CalendarDateStyle.Disabled
                        },
                        decorations = dateDecorations,
                        onClick = dateSelected,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CalendarMonthPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        CalendarMonth(
            year = 2024,
            month = Month.JANUARY,
            selection = LocalDate(2024, 1, 15),
            dateSelected = {},
            dateDecorations = { date, content ->
                content()
            },
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
        )
    }
}

@Preview
@Composable
fun CalendarMonthWithDecorationsPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        CalendarMonth(
            year = 2024,
            month = Month.MARCH,
            selection = LocalDate(2024, 3, 10),
            dateSelected = {},
            dateDecorations = { date, content ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                    // Add decoration dots for specific dates
                    if (date.dayOfMonth in listOf(5, 10, 15, 20, 25)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Canvas(
                                modifier = Modifier.size(4.dp)
                            ) {
                                drawCircle(color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            },
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
        )
    }
}
