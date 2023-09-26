package com.lift.bro.presentation.lift

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.ui.TopBar
import comliftbrodb.Lift
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import spacing

@Composable
fun EditLiftScreen(
    liftId: String? = null,
    liftSaved: () -> Unit,
    database: LBDatabase = dependencies.database,
) {
    val lift = database.liftDataSource.liftQueries.get(liftId ?: "").executeAsOneOrNull()

    var liftName by remember { mutableStateOf(lift?.name ?: "") }

    Scaffold(
        topBar = {
            TopBar(
                title = liftId?.let { "Edit Lift" } ?: "Create Lift",
                showBackButton = true
            )
        },
        floatingActionButton = {
            Button(
                enabled = liftName.isNotBlank(),
                onClick = {
                    database.liftDataSource.save(
                        Lift(
                            id = liftId ?: uuid4().toString(),
                            name = liftName,
                        )
                    )
                    liftSaved()
                }
            ) {
                Text("Save")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
            TextField(
                value = liftName,
                onValueChange = { liftName = it },
                placeholder = { Text("Lift Name. ex: Deadlift, Squat... Bulgarian Killjoy")}
            )
        }
    }
}