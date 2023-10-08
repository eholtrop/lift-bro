package com.lift.bro.presentation.set

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.variation.UOM
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Picker
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.VariationSelector
import com.lift.bro.ui.WeightSelector
import spacing

@Composable
fun EditSetScreen(
    setId: String,
    variationId: String,
    setSaved: () -> Unit,
) {
    val set = dependencies.database.setDataSource.get(setId)
        .executeAsOneOrNull()

    var variation by remember {
        mutableStateOf(
            dependencies.database.variantDataSource.get(set?.variationId ?: variationId)
                .executeAsOneOrNull()
        )
    }

    LiftingScaffold(
        fabText = "Create Set",
        fabClicked = setSaved,
        topBar = {
            TopBar(
                title = "",
                showBackButton = true,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VariationSelector(
                variation = variation,
                variationSelected = { variation = it }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))

            WeightSelector(
                weight = Pair(set?.weight, UOM.valueOf(set?.unit ?: UOM.POUNDS.toString())),
                weightChanged = { }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.two))

            TempoSelector(
                up = 1,
                hold = 1,
                down = 3,
            )
        }
    }
}

@Composable
fun TempoSelector(
    modifier: Modifier = Modifier,
    down: Int,
    hold: Int,
    up: Int,
) {
    Row {

        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Reps",
        )
        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Down",
            selectedNum = down - 1,
        )
        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Hold",
            selectedNum = hold - 1,
        )
        NumberPicker(
            modifier = Modifier.weight(.25f),
            title = "Up",
            selectedNum = up - 1,
        )
    }
}

@Composable
fun NumberPicker(
    modifier: Modifier,
    title: String,
    selectedNum: Int = 0,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(text = title)

        Picker(
            modifier = Modifier.weight(.33f),
            items = (1..99).toList().map { it.toString() },
            startIndex = selectedNum - 1,
        )
    }
}

enum class TimeUnit {
    Minutes, Hours;
}

@Composable
fun RestSelector(
    time: Int,
    unit: TimeUnit,
) {

}