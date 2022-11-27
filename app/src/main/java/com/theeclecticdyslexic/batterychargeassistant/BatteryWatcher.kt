package com.theeclecticdyslexic.batterychargeassistant

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.ContentInfoCompat.Flags
import java.util.*

class BatteryWatcher : BroadcastReceiver() {

    companion object {
        val SINGLETON = BatteryWatcher()

        var batteryMeasurements: TreeMap<Long, Float> = TreeMap()
        var lastPercent = -1f
        var chargeHandled = false
    }

    fun beginListening(context: Context) {
        initBroadcastReceivers(context)
        if (Utils.isPlugged(context)) {
            onPowerConnected(context)
        }
    }

    fun stopListening(context: Context) {
        try {
            context.unregisterReceiver(SINGLETON)
        } catch (e: Exception) {
            Log.d("Exception Occurred", "While trying to destroy service $e")
        }
    }

    private fun resetBatteryMeasurements(context: Context) {
        batteryMeasurements = TreeMap()
        lastPercent = Utils.batteryPercentage(context)
        chargeHandled = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> SINGLETON.onPowerConnected(context)

            Intent.ACTION_POWER_DISCONNECTED,
            Constants.ACTION_OVERRIDE_BATTERY_WATCHER -> SINGLETON.onPowerDisconnected(context)

            Intent.ACTION_BATTERY_CHANGED -> SINGLETON.handleChargePercent(context)

            else -> Log.d("battery watcher received unhandled intent:","${intent.action}")
        }
    }

    private fun handleChargePercent(context: Context) {

        val percent = Utils.batteryPercentage(context)

        val target = Settings.ChargeTarget.retrieve(context)
        if (target <= percent && !chargeHandled) {
            chargeHandled = true
            handleFullyCharged(context)
        } else if (target > percent) {
            chargeHandled = false
        }

        if (lastPercent == percent) return
        lastPercent = percent
        val time = System.currentTimeMillis()
        batteryMeasurements[time] = percent
        Utils.debugHttpRequest(Pair("time", time), Pair("percent", percent))
    }

    private fun handleFullyCharged(context: Context) {
        val reminderEnabled = Settings.ReminderEnabled.retrieve(context)
        if (reminderEnabled) {
            pushReminder(context)
        }

        val alarmEnabled = Settings.AlarmEnabled.retrieve(context)
        if (alarmEnabled) {
            soundAlarm(context)
        }

        val httpRequestEnabled = Settings.HTTPRequestEnabled.retrieve(context)
        if (httpRequestEnabled) {
            // TODO send httpRequest
            sendHTTPRequests(context)
        }

        // no need to check if controls are active
        // if there is no notification to cancel, there shouldn't be an issue
        val manager = initControlsNotificationChannel(context)
        manager.cancel(Constants.CONTROLS_NOTIFICATION_CHANNEL)

        stopBatteryWatcher(context)
    }

    private fun pushReminder(context: Context) {
        val manager = initReminderNotificationChannel(context)
        val notification = buildReminderNotification(context)
        manager.notify(Constants.REMINDER_NOTIFICATION_CHANNEL, notification)
    }

    // TODO merge notification controls with alarm
    private fun soundAlarm(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(
            context,
            BatteryWatcher::class.java).apply {
            action = Constants.ACTION_SOUND_ALARM
            flags = Intent.FLAG_RECEIVER_FOREGROUND or
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME
        }
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)

        alarm.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1,
            pi
        )
    }

    private fun sendHTTPRequests(context: Context) {
        Utils.debugHttpRequest(Pair("plug_power", "off"))
    }

    private fun buildReminderNotification(context: Context) : Notification {

        val target = Settings.ChargeTarget.retrieve(context)

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "reminder")
            .setSilent(false)
            .setShowWhen(false)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Finished Charging")
            .setContentText("Your phone has reached ${target}%")
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notification.build()
    }

    private fun initReminderNotificationChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            "reminder",
            "Charging Finished",
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }

    private fun initBroadcastReceivers(context: Context) {
        Log.d("registering receivers", "started")
        try {
            val intents = arrayOf(
                Intent.ACTION_POWER_CONNECTED,
                Intent.ACTION_POWER_DISCONNECTED,
                Constants.ACTION_OVERRIDE_BATTERY_WATCHER)
            for (i in intents) {
                context.applicationContext.registerReceiver(SINGLETON, IntentFilter(i))
            }

            Log.d("registering receivers", "completed")
        } catch (e: Exception) {
            // nothing to do, they are already registered
            Log.d("failed to register receiver", "$e")
        }
    }

    private fun buildControlsNotification(context: Context) : Notification {

        val target = Settings.ChargeTarget.retrieve(context)

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)

        // TODO put the correct pending intent in here so that the intent can be grabbed
        val intentDisableWatcher = Intent(context, BatteryWatcher::class.java)
            .apply { action = Constants.ACTION_OVERRIDE_BATTERY_WATCHER }
        val piDisableWatcher = PendingIntent.getBroadcast(
            context,
            0,
            intentDisableWatcher,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "reminder")
            .setSilent(false)
            .setShowWhen(false)
            .setOngoing(false)
            .setAutoCancel(false)
            .setContentIntent(pi)
            .setSmallIcon(R.drawable.ic_baseline_edit_24)
            .setContentTitle("Currently Charging")
            .setContentText("Waiting to hit ${target}%")
            .addAction(R.drawable.ic_baseline_edit_24, "Override & charge to 100%", piDisableWatcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notification.build()
    }

    private fun initControlsNotificationChannel(context: Context): NotificationManager {
        val channel = NotificationChannel(
            "controls",
            "Charging Controls",
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return manager
    }

    private fun onPowerConnected(context: Context) {
        val enabled = Settings.Enabled.retrieve(context) // shouldn't be necessary
        if (!enabled) return

        startBatteryWatcher(context)

        val controls = Settings.ControlsEnabled.retrieve(context)
        if (controls) {
            val manager = initControlsNotificationChannel(context)
            val notification = buildControlsNotification(context)
            manager.notify(Constants.CONTROLS_NOTIFICATION_CHANNEL, notification)
        }

        // TODO show notification, if enabled
        Log.d("Charging State Change", "power connected")
        Utils.debugHttpRequest(Pair("power_connected", true))
    }

    private fun onPowerDisconnected(context: Context) {
        stopBatteryWatcher(context)
        var manager = initReminderNotificationChannel(context)
        manager.cancel(Constants.REMINDER_NOTIFICATION_CHANNEL)
        manager = initControlsNotificationChannel(context)
        manager.cancel(Constants.CONTROLS_NOTIFICATION_CHANNEL)
        // TODO cancel interactive notification
        Log.d("Charging State Change", "power disconnected")
        Utils.debugHttpRequest(Pair("power_connected", false))
    }

    private fun stopBatteryWatcher(context: Context) {
        try {
            context.unregisterReceiver(SINGLETON)
        } catch (e: Exception) {
            Log.d("failed to unregister receivers","$e")
        }
        initBroadcastReceivers(context)
    }

    private fun startBatteryWatcher(context: Context) {
        try {
            context.registerReceiver(
                SINGLETON,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            resetBatteryMeasurements(context)
        } catch (e : Exception) {
            // do nothing, already registered
        }
    }

}