package com.lift.bro.presentation.lift

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import comliftbrodb.Lift

@Composable
fun AddLiftScreen(
    database: LBDatabase = dependencies.database,
    liftSaved: () -> Unit
) {

    Column {

        var liftName by remember { mutableStateOf("") }

        TextField(
            value = liftName,
            onValueChange = { liftName = it }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                database.liftDataSource.save(
                    Lift(
                        id = uuid4().toString(),
                        name = liftName,
                    )
                )
                liftSaved()
            }
        ) {
            Text("Save")
        }
    }
}