package com.lift.bro.presentation.wrapped


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoSpeachBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.today
import com.lift.bro.utils.vertical_padding.padding
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedTenureScreen(
    state: WrappedPageState.Tenure,
) {
    val currentYear = today.year

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
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                if (currentYear == state.year) {
                    FadeInText(
                        delay = 100L,
                        text = stringResource(Res.string.wrapped_tenure_first_year_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Space(MaterialTheme.spacing.one)
                    FadeInText(
                        delay = 200L,
                        text = stringResource(Res.string.wrapped_tenure_first_year_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Space(MaterialTheme.spacing.two)

                    InfoSpeachBubble(
                        title = {
                            FadeInText(
                                delay = 300L,
                                text = stringResource(Res.string.wrapped_tenure_first_year_speech_bubble_title),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        },
                        message = {
                            FadeInText(
                                delay = 400L,
                                text = stringResource(Res.string.wrapped_tenure_first_year_speech_bubble_message),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    )
                } else {
                    FadeInText(
                        delay = 500L,
                        text = stringResource(Res.string.wrapped_tenure_veteran_title, currentYear - state.year),
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

