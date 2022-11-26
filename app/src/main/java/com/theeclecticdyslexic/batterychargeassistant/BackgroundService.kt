package com.theeclecticdyslexic.batterychargeassistant

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat


class BackgroundService : Service() {
    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.d("starting service", "background service")
        Utils.debugHttpRequest(Pair("starting_service", true))

        val enabled = Settings.Enabled.retrieve(this)
        try {
            if (enabled) {
                BatteryWatcher.SINGLETON.initBroadcastReceivers(this)
            }
        } catch (e : Exception) {
            Log.d("Exception Occurred", "While trying to start service $e")
        }

        startForeground(FOREGROUND_NOTIFICATION_ID, buildOngoingNotification())

        return START_STICKY
    }

    override fun onDestroy() {
        Utils.debugHttpRequest(Pair("stopping_service", true))
        try {
            unregisterReceiver(BatteryWatcher.SINGLETON)
        } catch (e: Exception) {
            Log.d("Exception Occurred", "While trying to destroy service $e")
        }
    }


    private fun buildOngoingNotification() : Notification {

        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "persistent")
            .setSilent(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Running in the Background")
            .setContentText("This notification can be hidden in notification settings")
            .setPriority(NotificationCompat.PRIORITY_MIN)

        return notification.build()
    }

    private fun initPersistentNotificationChannel(): NotificationManager {
        val channel = NotificationChannel(
                "persistent",
                "Persistent Notification",
                NotificationManager.IMPORTANCE_MIN
        )

        val manager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }

    override fun onCreate(){
        initPersistentNotificationChannel()
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
        // TODO how to write IBinder
    }


}