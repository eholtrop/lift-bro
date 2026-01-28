package com.lift.bro.presentation.wrapped

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import tv.dpal.flowvi.Interactor
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import tv.dpal.flowvi.rememberInteractor
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoSpeechBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.`ktx-datetime`.toLocalDate
import com.lift.bro.compose.vertical_padding.padding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import lift_bro.core.generated.resources.*
import lift_bro.core.generated.resources.Res
import org.jetbrains.compose.resources.stringResource

@Serializable
data class WrappedTenureState(
    val year: Int,
    val tenure: Int,
)

/*
 * Fetches how long the user has been using Lift Bro in *years*
 */
class GetUserTenureUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
) {
    operator fun invoke(year: Int): Flow<Int> = setRepository.listenAll(
        order = Order.Ascending,
        sorting = Sorting.date,
        limit = 1
    ).map {
        year - (it.minOfOrNull { it.date.toLocalDate() }?.year ?: year)
    }
}

@Composable
fun rememberWrappedTenureInteractor(
    year: Int = LocalWrappedYear.current,
    getUserTenureUseCase: GetUserTenureUseCase = GetUserTenureUseCase(),
) = rememberInteractor<WrappedTenureState?, Nothing>(
    initialState = null,
    source = {
        getUserTenureUseCase(year)
            .map { WrappedTenureState(year, it) }
    }
)

@Composable
fun WrappedTenureScreen(
    interactor: Interactor<WrappedTenureState?, Nothing> = rememberWrappedTenureInteractor(),
) {
    val state by interactor.state.collectAsState()

    state?.let { WrappedTenureScreen(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedTenureScreen(
    state: WrappedTenureState,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(MaterialTheme.spacing.one),
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
                text = stringResource(Res.string.wrapped_tenure_header_title),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.tenure == 0) {
                    FadeInText(
                        delay = 100L,
                        text = stringResource(Res.string.wrapped_tenure_first_year_title, state.year),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Space(MaterialTheme.spacing.one)
                    FadeInText(
                        delay = 200L,
                        text = stringResource(Res.string.wrapped_tenure_first_year_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Space(MaterialTheme.spacing.two)

                    InfoSpeechBubble(
                        title = {
                            FadeInText(
                                delay = 300L,
                                text = stringResource(Res.string.wrapped_tenure_first_year_speech_bubble_title),
                                style = MaterialTheme.typography.displaySmall
                            )
                        },
                        message = {
                            FadeInText(
                                delay = 400L,
                                text = stringResource(Res.string.wrapped_tenure_first_year_speech_bubble_message),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    )
                } else {
                    FadeInText(
                        delay = 500L,
                        text = stringResource(Res.string.wrapped_tenure_veteran_title, state.tenure),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Space(MaterialTheme.spacing.one)
                    FadeInText(
                        delay = 600L,
                        text = stringResource(Res.string.wrapped_tenure_veteran_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))
        }

        item {
            if (LocalSubscriptionStatusProvider.current.value == SubscriptionType.Pro) {
                FadeInText(
                    delay = 800L,
                    text = stringResource(Res.string.wrapped_tenure_pro_thanks_title),
                    style = MaterialTheme.typography.titleLarge
                )
                FadeInText(
                    delay = 1000L,
                    text = stringResource(Res.string.wrapped_tenure_pro_thanks_subtitle),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
