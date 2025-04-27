package com.lift.bro.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import com.lift.bro.data.Backup
import com.lift.bro.di.DependencyContainer
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.App
import com.lift.bro.presentation.StoreManager
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.rememberNavCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DependencyContainer.context = this
        StoreManager.context = this
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val coordinator = rememberNavCoordinator(Destination.Dashboard)
            App(
                navCoordinator = coordinator
            )

            BackHandler {
                if (!coordinator.onBackPressed()) {
                    onBackPressed()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        DependencyContainer.context = null
        StoreManager.context = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            data?.data?.also {

                contentResolver.openInputStream(it)?.readBytes()?.toString(StandardCharsets.UTF_8)
                    ?.let { json ->
                        GlobalScope.launch {
                            val backup = Json.decodeFromString<Backup>(json)

                            dependencies.database.liftDataSource.deleteAll()
                            dependencies.database.variantDataSource.deleteAll()
                            dependencies.database.setDataSource.deleteAll()

                            backup.lifts.forEach {
                                dependencies.database.liftDataSource.save(it)
                            }

                            backup.variations.forEach {
                                dependencies.database.variantDataSource.save(
                                    id = it.id,
                                    liftId = it.lift!!.id,
                                    name = it.name,
                                )
                            }

                            backup.sets.forEach {
                                dependencies.database.setDataSource.save(it)
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Backup Restored!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }
    }
}
