package com.lift.bro.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lift.bro.core.R
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.server.PresentationDataSources
import com.lift.bro.presentation.server.startPresentationServer

class LiftBroServerForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val port = intent.getIntExtra(EXTRA_PORT, 8080)
                startForegroundWithNotification()
                startServer(port)
                saveEnabled(this, true)
            }
            ACTION_STOP -> {
                stopServer()
                saveEnabled(this, false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, LiftBroServerForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)
        val openAppPending = PendingIntent.getActivity(
            this, 1, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lift Bro server running")
            .setContentText("Tap to open. Stop to shut down the server.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppPending)
            .addAction(0, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startServer(port: Int) {
        if (serverHandle != null) return
        val data = PresentationDataSources(
            lifts = com.lift.bro.data.sqldelight.datasource.SqldelightLiftDataSource(dependencies.database.liftQueries),
            sets = com.lift.bro.data.sqldelight.datasource.SqldelightSetDataSource(dependencies.database.setQueries),
            exercises = com.lift.bro.data.sqldelight.datasource.SqldelightExerciseDataSource(
                exerciseQueries = dependencies.database.exerciseQueries,
                setQueries = dependencies.database.setQueries,
                variationQueries = dependencies.database.variationQueries,
            ),
            variations = com.lift.bro.data.sqldelight.datasource.SqlDelightVariationDataSource(
                liftQueries = dependencies.database.liftQueries,
                setQueries = dependencies.database.setQueries,
                variationQueries = dependencies.database.variationQueries,
            ),
            workouts = com.lift.bro.data.sqldelight.datasource.SqldelightWorkoutDataSource(
                workoutQueries = dependencies.database.workoutQueries,
            ),
            settings = dependencies.settingsRepository,
        )
        serverHandle = startPresentationServer(port = port, dataSources = data)
    }

    private fun stopServer() {
        serverHandle?.stop()
        serverHandle = null
    }

    companion object {
        private const val CHANNEL_ID = "lift_bro_server_channel"
        private const val CHANNEL_NAME = "Lift Bro Server"
        private const val NOTIFICATION_ID = 42

        const val ACTION_START = "com.lift.bro.server.action.START"
        const val ACTION_STOP = "com.lift.bro.server.action.STOP"
        const val EXTRA_PORT = "extra_port"

        private var serverHandle: com.lift.bro.presentation.server.PresentationServer? = null

        private const val PREFS = "lift_bro_prefs"
        private const val KEY_ENABLED = "server_enabled"

        fun isEnabled(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

        fun saveEnabled(context: Context, enabled: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
        }
    }
}
