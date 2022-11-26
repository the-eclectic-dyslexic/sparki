package com.theeclecticdyslexic.batterychargeassistant

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import java.util.*

object BatteryWatcher : BroadcastReceiver() {

    var batteryMeasurements : TreeMap<Long, Float> = TreeMap()
    var lastPercent = -1f
    var chargeHandled = false

    private fun resetBatteryMeasurements(context: Context) {
        batteryMeasurements = TreeMap()
        lastPercent = Utils.batteryPercentage(context)
        chargeHandled = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> onPowerConnected(context)
            Intent.ACTION_POWER_DISCONNECTED -> onPowerDisconnected(context)
            Intent.ACTION_BATTERY_CHANGED -> handleChargePercent(context)
            else -> Log.d("battery watcher received unhandled intent","received ${intent.action}")
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
            // TODO push reminder
            val manager = initReminderNotificationChannel(context)
            val notification = buildReminderNotification(context)
            manager.notify(Constants.REMINDER_NOTIFICATION_CHANNEL, notification)
        }

        val alarmEnabled = Settings.AlarmEnabled.retrieve(context)
        if (alarmEnabled) {
            // TODO sound alarm
        }

        val httpRequestEnabled = Settings.HTTPRequestEnabled.retrieve(context)
        if (httpRequestEnabled) {
            // TODO send httpRequest
            Utils.debugHttpRequest(Pair("plug_power", "off"))
        }

    }

    private fun buildReminderNotification(context: Context) : Notification {

        val target = Settings.ChargeTarget.retrieve(context)

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

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

    fun initializeReceivers(context: Context) {
        Log.d("registering receivers", "started")
        try {
            val f = IntentFilter(Intent.ACTION_POWER_CONNECTED)
            val f2 = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
            context.registerReceiver(this, f)
            context.registerReceiver(this, f2)
            Log.d("registering receivers", "completed")
        } catch (e: Exception) {
            // nothing to do, they are already registered
        }
    }

    private fun onPowerConnected(context: Context) {
        resetBatteryMeasurements(context)
        val enabled = Settings.Enabled.retrieve(context)
        if (!enabled) return

        startBatteryWatcher(context)
        // TODO show notification, if enabled
        Log.d("Charging State Change", "power connected")
        Utils.debugHttpRequest(Pair("power_connected", true))
    }

    private fun onPowerDisconnected(context: Context) {
        stopBatteryWatcher(context)
        // TODO hide notification, if enabled
        Log.d("Charging State Change", "power disconnected")
        Utils.debugHttpRequest(Pair("power_connected", false))
    }

    private fun stopBatteryWatcher(context: Context) {
        context.unregisterReceiver(this)
        initializeReceivers(context)
    }

    private fun startBatteryWatcher(context: Context) {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(this, filter)
        } catch (e : Exception) {
            // do nothing, already registered
        }
    }

}