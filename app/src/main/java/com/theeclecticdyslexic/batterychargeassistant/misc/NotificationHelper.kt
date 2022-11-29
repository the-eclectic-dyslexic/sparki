package com.theeclecticdyslexic.batterychargeassistant.misc

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.theeclecticdyslexic.batterychargeassistant.R
import com.theeclecticdyslexic.batterychargeassistant.background.MainReceiver
import com.theeclecticdyslexic.batterychargeassistant.ui.MainActivity

object NotificationHelper {

    private enum class Channel {
        Reminder,
        Controls,
        Sticky
    }

    fun pushReminder(context: Context) {
        val manager = initReminderChannel(context)
        val notification = buildReminder(context)
        manager.notify(Channel.Reminder.ordinal, notification)
    }

    fun cancelReminder(context: Context) =
        initControlsChannel(context)
            .cancel(Channel.Reminder.ordinal)

    private var controlsActive = false
    fun pushControls(context: Context) {
        if (controlsActive) return
        controlsActive = true
        val manager = initControlsChannel(context)
        val notification = buildControls(context)
        manager.notify(Channel.Controls.ordinal, notification)
    }

    fun cancelControls(context: Context) {
        controlsActive = false

        initReminderChannel(context)
            .cancel(Channel.Controls.ordinal)
    }


    fun pushSticky(service: Service){
        initStickyChannel(service)
        service.startForeground(Channel.Sticky.ordinal, buildSticky(service))
    }

    private fun buildControls(context: Context) : Notification {

        val target = Settings.ChargeTarget.retrieve(context)

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)

        // TODO put the correct pending intent in here so that the intent can be grabbed
        val intentDisableWatcher = Intent(context, MainReceiver::class.java)
            .apply { action = Action.OVERRIDE_WATCHDOG.id }
        val piDisableWatcher = PendingIntent.getBroadcast(
            context,
            0,
            intentDisableWatcher,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "reminder")
            .setSilent(false)
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Currently Charging")
            .setContentText("Waiting to hit ${target}%")
            .addAction(R.drawable.ic_baseline_edit_24, "Override & charge to 100%", piDisableWatcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notification.build()
    }

    private fun initControlsChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            "controls",
            "Charging Controls",
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }

    private fun buildReminder(context: Context) : Notification {

        val target = Settings.ChargeTarget.retrieve(context)

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "reminder")
            .setSilent(false)
            .setShowWhen(true)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Finished Charging")
            .setContentText("Your device has reached ${target}%")
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notification.build()
    }

    private fun initReminderChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            "reminder",
            "Charging Finished",
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }

    private fun buildSticky(context: Context) : Notification {

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "persistent")
            .setSilent(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Running in the Background")
            .setContentText("context notification can be hidden in notification settings")
            .setPriority(NotificationCompat.PRIORITY_MIN)

        return notification.build()
    }

    private fun initStickyChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            "persistent",
            "Persistent Notification",
            NotificationManager.IMPORTANCE_MIN
        )

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }
}