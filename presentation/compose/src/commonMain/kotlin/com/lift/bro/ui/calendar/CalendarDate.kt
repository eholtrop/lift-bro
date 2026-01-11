package com.lift.bro.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun CalendarDate(
    modifier: Modifier = Modifier,
    date: LocalDate,
    style: CalendarDateStyle,
    onClick: (LocalDate) -> Unit,
    decorations: @Composable (LocalDate, @Composable () -> Unit) -> Unit,
) {
    val backgroundColor = when (style) {
        CalendarDateStyle.Selected -> MaterialTheme.colorScheme.tertiary
        CalendarDateStyle.Enabled -> MaterialTheme.colorScheme.surface
        CalendarDateStyle.Disabled -> MaterialTheme.colorScheme.surfaceDim
        CalendarDateStyle.Today -> MaterialTheme.colorScheme.secondary
    }

    val contentColor = when (style) {
        CalendarDateStyle.Selected -> MaterialTheme.colorScheme.onTertiary
        CalendarDateStyle.Enabled -> MaterialTheme.colorScheme.onSurface
        CalendarDateStyle.Disabled -> MaterialTheme.colorScheme.onSurface
        CalendarDateStyle.Today -> MaterialTheme.colorScheme.onSecondary
    }

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        Color.Transparent,
                    )
                ),
                shape = MaterialTheme.shapes.small,
            )
            .clickable {
                onClick(date)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            decorations(date) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

enum class CalendarDateStyle {
    Enabled, Disabled, Selected, Today,
}

@Preview
@Composable
fun CalendarDatePreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            // Enabled date
            CalendarDate(
                modifier = Modifier.size(60.dp),
                date = LocalDate(2024, 1, 15),
                style = CalendarDateStyle.Enabled,
                onClick = {},
                decorations = { date, content ->
                    content()
                }
            )

            // Today date
            CalendarDate(
                modifier = Modifier.size(60.dp),
                date = today,
                style = CalendarDateStyle.Today,
                onClick = {},
                decorations = { date, content ->
                    content()
                }
            )

            // Selected date
            CalendarDate(
                modifier = Modifier.size(60.dp),
                date = LocalDate(2024, 1, 20),
                style = CalendarDateStyle.Selected,
                onClick = {},
                decorations = { date, content ->
                    content()
                }
            )

            // Disabled date
            CalendarDate(
                modifier = Modifier.size(60.dp),
                date = LocalDate(2024, 2, 1),
                style = CalendarDateStyle.Disabled,
                onClick = {},
                decorations = { date, content ->
                    content()
                }
            )
        }
    }
}
