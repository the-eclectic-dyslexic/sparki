/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.theeclecticdyslexic.sparki.R
import com.theeclecticdyslexic.sparki.ui.MainActivity

/**
 * A set of functions used to simplify notification pushing/cancelling
 *
 * Largely used to contain builder patterns so they are not found amidst program logic
 */

object NotificationHelper {

    private const val STATUS_ICON = R.drawable.ic_sparki

    enum class Channel(val description: String) {
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

    fun pushControls(context: Context) {
        val manager = initControlsChannel(context)
        val notification = buildControls(context)
        manager.notify(Channel.Controls.ordinal, notification)
    }

    fun cancelControls(context: Context) {
        initReminderChannel(context)
            .cancel(Channel.Controls.ordinal)
    }

    private fun buildControls(context: Context) : Notification {
        val target = Settings.ChargeTarget.retrieve(context)

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
            .addLinkToMain(context)
            .setSmallIcon(STATUS_ICON)
            .setContentTitle("Currently Charging")
            .setContentText("Sparki will let you know when the battery reaches ${target}%")
            .addAction(STATUS_ICON, "Let Sparki rest this time", piDisableWatcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notification.build()
    }

    private fun NotificationCompat.Builder.addLinkToMain(context: Context): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)
        return setContentIntent(pi)
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

        val notification = NotificationCompat.Builder(context, Channel.Reminder.name)
            .setSilent(false)
            .setShowWhen(true)
            .setOngoing(false)
            .setAutoCancel(false)
            .addLinkToMain(context)
            .addDismissAction(context)
            .setSmallIcon(STATUS_ICON)
            .setContentTitle("Time To Stop Charging")
            .setContentText("Your device has reached ${target}%")
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notification.build()
    }

    private fun NotificationCompat.Builder.addDismissAction(context: Context): NotificationCompat.Builder {
        val intent = Intent(Action.OVERRIDE_WATCHDOG.id)
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)
        this.addAction(STATUS_ICON, "Sparki, shush", pi)
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

    fun buildSticky(context: Context) : Notification {
        val notification = NotificationCompat.Builder(context, Channel.Sticky.name)
            .setSilent(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .addLinkToMain(context)
            .setSmallIcon(STATUS_ICON)
            .setContentTitle("Sparki is working in the background (like a good boy)")
            .setContentText("You can hide this notification in system settings.")
            .addNotificationSettingsAction(context)
            .setPriority(NotificationCompat.PRIORITY_MIN)

        return notification.build()
    }

    private fun NotificationCompat.Builder.addNotificationSettingsAction(context: Context): NotificationCompat.Builder {
        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        this.addAction(STATUS_ICON, "Take me there", pi)
        return this
    }

    fun initStickyChannel(context: Context): NotificationManager {
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