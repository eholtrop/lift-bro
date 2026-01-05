package com.lift.bro.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("StaticFieldLeak")
actual object StoreManager {

    var context: Context? = null

    private val appUpdateManager: AppUpdateManager get() = AppUpdateManagerFactory.create(context!!)

    actual fun isUpdateAvailable(): Flow<Boolean> = flow {
        val appInfo = appUpdateManager.appUpdateInfo.await()
        if (appInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appInfo.isUpdateTypeAllowed(
                AppUpdateType.IMMEDIATE
            )
        ) {
            emit(true)
        } else {
            emit(false)
        }
    }

    actual fun startUpdateFlow() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            appUpdateManager.startUpdateFlow(
                info,
                context as Activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
            )
        }
    }
}

suspend fun <T> Task<T>.await(): T = suspendCoroutine { cont ->
    addOnSuccessListener { result ->
        cont.resume(result)
    }
}
