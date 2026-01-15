package com.lift.bro.presentation.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.defaultSbdLifts
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.RadioButtonCard
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.fullName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.onboarding_setup_screen_continue_cta
import lift_bro.core.generated.resources.onboarding_setup_screen_select_lifts_title
import lift_bro.core.generated.resources.onboarding_setup_screen_select_uom_title
import lift_bro.core.generated.resources.onboarding_setup_screen_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingSetupScreen(
    formSubmitted: () -> Unit
) {
    var uom by remember { mutableStateOf(UOM.POUNDS) }
    val selectedVariations = remember { mutableStateListOf<Variation>() }

    Column(
        modifier = Modifier
            .onboardingBackground()
            .navigationBarsPadding()
            .statusBarsPadding()
            .padding(
                horizontal = MaterialTheme.spacing.one,
                vertical = MaterialTheme.spacing.two,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.onboarding_setup_screen_title),
            style = MaterialTheme.typography.displayLarge
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
        ) {
            item {
                Text(
                    modifier = Modifier.semantics { heading() },
                    text = stringResource(Res.string.onboarding_setup_screen_select_lifts_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Space(MaterialTheme.spacing.one)
            }

            item {
                OnboardingLiftSelector(selectedVariations)
                Space(MaterialTheme.spacing.two)
            }
            item {
                Text(
                    modifier = Modifier.semantics { heading() },
                    text = stringResource(Res.string.onboarding_setup_screen_select_uom_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Space(MaterialTheme.spacing.one)
            }
            item {
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
                ) {
                    Space(MaterialTheme.spacing.two)
                    RadioButtonCard(
                        modifier = Modifier.weight(1f)
                            .aspectRatio(1f),
                        backgroundColor = Color.White,
                        onClick = { uom = UOM.POUNDS },
                        selected = uom == UOM.POUNDS,
                    ) {
                        Text(
                            "LBS",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }

                    RadioButtonCard(
                        modifier = Modifier.weight(1f)
                            .aspectRatio(1f),
                        backgroundColor = Color.White,
                        onClick = { uom = UOM.KG },
                        selected = uom == UOM.KG,
                    ) {
                        Text(
                            "KG",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                    Space(MaterialTheme.spacing.two)
                }
                Space(MaterialTheme.spacing.two)
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            dependencies.settingsRepository.saveUnitOfMeasure(
                                Settings.UnitOfWeight(
                                    uom
                                )
                            )

                            GlobalScope.launch {
                                selectedVariations.toList().forEach {
                                    dependencies.database.variantDataSource.save(it)
                                }
                                selectedVariations.toList().map { it.lift }.filterNotNull()
                                    .forEach {
                                        dependencies.database.liftDataSource.save(it)
                                    }
                                delay(100)
                                formSubmitted()
                            }
                        },
                        colors = ButtonDefaults.elevatedButtonColors(),
                    ) {
                        Text(
                            stringResource(Res.string.onboarding_setup_screen_continue_cta),
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingLiftSelector(
    selectedVariations: SnapshotStateList<Variation>,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = Color.White,
                shape = MaterialTheme.shapes.large,
            ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
    ) {
        with(defaultSbdLifts) {
            lifts?.forEachIndexed { index, lift ->
                var expanded by remember { mutableStateOf(false) }

                val liftVariations = variations?.filter { it.lift?.id == lift.id }

                Column(
                    modifier = Modifier.animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.clickable {
                            expanded = !expanded
                        }.padding(
                            top = if (index == 0) MaterialTheme.spacing.one else 0.dp,
                            start = MaterialTheme.spacing.one,
                            end = MaterialTheme.spacing.one,
                            bottom = if (index == lifts?.lastIndex) MaterialTheme.spacing.one else 0.dp
                        ).defaultMinSize(minHeight = Dp.AccessibilityMinimumSize),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                lift.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row {
                                liftVariations?.forEachIndexed { index, variation ->
                                    val selected by remember {
                                        derivedStateOf {
                                            selectedVariations.contains(
                                                variation
                                            )
                                        }
                                    }
                                    Text(
                                        if (index == variations.size - 1) {
                                            variation.name
                                                ?: ""
                                        } else {
                                            "${variation.name}, "
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Image(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = ""
                        )
                    }

                    val coroutineScope = rememberCoroutineScope()
                    if (expanded) {
                        liftVariations?.forEach { variation ->
                            val checked by remember {
                                derivedStateOf {
                                    selectedVariations.contains(
                                        variation
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable(
                                        role = Role.Checkbox,
                                        onClick = {
                                            if (checked) {
                                                selectedVariations.remove(variation)
                                            } else {
                                                selectedVariations.add(variation)
                                            }
                                        }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        coroutineScope.launch {
                                            if (it) {
                                                selectedVariations.add(variation)
                                            } else {
                                                selectedVariations.remove(variation)
                                            }
                                        }
                                    }
                                )
                                Text(variation.fullName)
                            }
                        }
                    }
                }
            }
        }
    }
}
