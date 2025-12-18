package com.lift.bro.presentation.wrapped


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrappedTenureScreen(
    state: WrappedPageState.Tenure,
) {
    val currentYear = today.year

    LiftingScaffold(
        title = { Text("Welcome to Lift Bro Wrapped!") },
        content = { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(MaterialTheme.spacing.one),
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        if (currentYear == state.year) {
                            FadeInText(
                                delay = 100L,
                                text = "This was your first Lift Bro year! \uD83C\uDF89",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Space(MaterialTheme.spacing.one)
                            FadeInText(
                                delay = 200L,
                                text = "Whether you started using lift bro this year, or started on your lifting journey",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Space(MaterialTheme.spacing.two)

                            InfoSpeachBubble(
                                title = {
                                    FadeInText(
                                        delay = 300L,
                                        text = "Thank YOU!!!",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                },
                                message = {
                                    FadeInText(
                                        delay = 400L,
                                        text = "Here's to a great next year! \uD83E\uDD73 \uD83E\uDD42",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            )
                        } else {
                            FadeInText(
                                delay = 500L,
                                text = "You've been a lift bro for ${currentYear - state.year} years!",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Space(MaterialTheme.spacing.one)
                            FadeInText(
                                delay = 600L,
                                text = "Congrats!! and thank YOU for the support",
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
                            text = "And a VERY EXTRA SPECIAL THANK YOU! for being a Lift PRO!!!",
                            style = MaterialTheme.typography.titleLarge
                        )
                        FadeInText(
                            delay = 1000L,
                            text = "Support like yours is what keeps this app going!",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    )
}

