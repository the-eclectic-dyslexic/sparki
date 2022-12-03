package com.theeclecticdyslexic.batterychargeassistant.misc

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.theeclecticdyslexic.batterychargeassistant.R
import com.theeclecticdyslexic.batterychargeassistant.ui.MainActivity

object NotificationHelper {

    private enum class Channel(val description: String) {
        Reminder("Finished charging reminder"),
        Controls("In-notification controls"),
        Sticky  ("Background status notification")
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

        val intentDisableWatcher = Intent(Action.OVERRIDE_WATCHDOG.id)
        val piDisableWatcher = PendingIntent.getBroadcast(
            context,
            0,
            intentDisableWatcher,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, Channel.Controls.name)
            .setSilent(false)
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Currently Charging")
            .setContentText("Sparki will let you know when the battery reaches ${target}%")
            .addAction(R.drawable.ic_baseline_edit_24, "Let Sparki rest this time", piDisableWatcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notification.build()
    }

    private fun initControlsChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            Channel.Controls.name,
            Channel.Controls.description,
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

        val notification = NotificationCompat.Builder(context, Channel.Reminder.name)
            .setSilent(false)
            .setShowWhen(true)
            .setOngoing(false)
            .setAutoCancel(false)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Time To Stop Charging")
            .setContentText("Your device has reached ${target}%")
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val alarm = Settings.AlarmEnabled.retrieve(context)
        if (alarm) notification.addDismissAction(context) // TODO test this thoroughly with the phone on silent and vibrate

        return notification.build()
    }

    private fun NotificationCompat.Builder.addDismissAction(context: Context): NotificationCompat.Builder {
        val intent = Intent(Action.OVERRIDE_WATCHDOG.id)
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)
        this.addAction(R.drawable.ic_baseline_edit_24, "Sparki, shush", pi)
        return this
    }

    private fun initReminderChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            Channel.Reminder.name,
            Channel.Reminder.description,
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }

    private fun buildSticky(context: Context) : Notification {

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, Channel.Sticky.name)
            .setSilent(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Sparki is working in the background (like a good boy)")
            .setContentText("This notification can be permanently hidden in notification settings.")
            .addNotificationSettingsAction(context)
            .setPriority(NotificationCompat.PRIORITY_MIN)

        return notification.build()
    }

    private fun NotificationCompat.Builder.addNotificationSettingsAction(context: Context): NotificationCompat.Builder {
        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        this.addAction(R.drawable.ic_baseline_edit_24, "Take me there", pi)
        return this
    }

    private fun initStickyChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            Channel.Sticky.name,
            Channel.Sticky.description,
            NotificationManager.IMPORTANCE_MIN
        )

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }
}