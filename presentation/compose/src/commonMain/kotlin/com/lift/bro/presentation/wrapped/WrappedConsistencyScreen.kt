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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.dialog.InfoSpeachBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.vertical_padding.padding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Serializable
@Immutable
data class WrappedConsistencyState(
    val dates: Set<LocalDate>,
)

class GetVariationConsistencyUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
    val variationRepository: IVariationRepository = dependencies.variationRepository,
) {

    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Map<LocalDate, List<Variation>>> = combine(
        setRepository.listenAll(
            startDate = startDate,
            endDate = endDate
        ),
        variationRepository.listenAll()
    ) { sets, variations ->
        sets.groupBy { set -> set.date.toLocalDate() }
            .mapValues { (_, sets) ->
                variations.filter { variation -> sets.any { it.variationId == variation.id } }
            }
    }
}

@Composable
fun rememberWrappedConsistencyInteractor(
    getVariationConsistencyUseCase: GetVariationConsistencyUseCase = GetVariationConsistencyUseCase(),
) = rememberInteractor<WrappedConsistencyState, Nothing>(
    initialState = WrappedConsistencyState(dates = emptySet()),
    source = {
        getVariationConsistencyUseCase()
            .map {
                WrappedConsistencyState(
                    dates = it.keys
                )
            }

    }
)

@Composable
fun WrappedConsistencyScreen(
    interactor: Interactor<WrappedConsistencyState, Nothing> = rememberWrappedConsistencyInteractor(),
) {
    val state by interactor.state.collectAsState()

    WrappedConsistencyScreen(state)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedConsistencyScreen(
    page: WrappedConsistencyState,
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
                text = "And look at how consistent you were!",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        val dates = page.dates.groupBy { it.month }

        item(
            span = { GridItemSpan(2) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                if (dates.isNotEmpty()) {
                    InfoSpeachBubble(
                        title = {
                            Text(
                                "Way to go!",
                                style = MaterialTheme.typography.displaySmall
                            )
                        },
                        message = {
                            Text(
                                text = "Your most consistent month was ${dates.maxBy { it.value.size }.key.name} with ${dates.maxBy { it.value.size }.value.size} days!",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                            )
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
                    text = it.name.first().toString()
                )
            }
        }
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
                                        color = if (days.contains(day.dayOfMonth)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
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
            page = WrappedConsistencyState(
                dates = setOf(
                    today,
                    LocalDate(today.year, 1, 1)
                )
            )
        )
    }
}
