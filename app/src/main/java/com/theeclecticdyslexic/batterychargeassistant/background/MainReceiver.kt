package com.theeclecticdyslexic.batterychargeassistant.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log
import com.theeclecticdyslexic.batterychargeassistant.misc.*
import java.util.*


class MainReceiver : BroadcastReceiver() {

    companion object {
        val SINGLETON = MainReceiver()
    }

    private var batteryMeasurements: TreeMap<Long, Float> = TreeMap()
    private var lastPercent = -1f
    private var chargeHandled = false
    private lateinit var ringer: Ringtone

    private val alwaysReceiving = listOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Action.OVERRIDE_WATCHDOG.id,
        Action.STOP_ALARM.id
    )

    fun beginReceiving(context: Context) {
        initBroadcastReceivers(context)
        if (Utils.isPlugged(context)) {
            onPowerConnected(context)
        }
        initRinger(context)
    }

    fun stopReceiving(context: Context) {
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
            Action.OVERRIDE_WATCHDOG.id -> SINGLETON.onPowerDisconnected(context)

            Intent.ACTION_BATTERY_CHANGED -> SINGLETON.handleChargePercent(context)

            Action.STOP_ALARM.id -> playRinger()

            else -> Log.d("battery watcher received unhandled intent:","${intent.action}")
        }
    }

    private fun initRinger(context: Context){
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringer = RingtoneManager.getRingtone(context, alarmUri)
        ringer.audioAttributes =
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build()
    }

    private fun playRinger() {
        Debug.logOverREST(Pair("alarm", "starting"))
        ringer.play()
    }

    private fun stopRinger() {
        Debug.logOverREST(Pair("alarm", "stopping"))
        ringer.stop()
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
        Debug.logOverREST(Pair("time", time), Pair("percent", percent))
    }

    private fun handleFullyCharged(context: Context) {
        val reminderEnabled = Settings.ReminderEnabled.retrieve(context)
        if (reminderEnabled) {
            NotificationHelper.pushReminder(context)
        }

        val alarmEnabled = Settings.AlarmEnabled.retrieve(context)
        if (alarmEnabled) {
            playRinger()
        }

        val httpRequestEnabled = Settings.HTTPRequestEnabled.retrieve(context)
        if (httpRequestEnabled) {
            // TODO send httpRequest
            sendHTTPRequests()
        }

        NotificationHelper.cancelControls(context)

        stopBatteryWatcher(context)
    }

    private fun sendHTTPRequests() {
        Debug.logOverREST(Pair("plug_power", "off"))
    }

    private fun initBroadcastReceivers(context: Context) {
        Log.d("registering receivers", "started")
        try {
            val intents = alwaysReceiving
            for (i in intents) {
                context.applicationContext.registerReceiver(SINGLETON, IntentFilter(i))
            }

            Log.d("registering receivers", "completed")
        } catch (e: Exception) {
            // nothing to do, they are already registered
            Log.d("failed to register receiver", "$e")
        }
    }

    private fun onPowerConnected(context: Context) {
        val enabled = Settings.Enabled.retrieve(context) // shouldn't be necessary
        if (!enabled) return

        startBatteryWatcher(context)

        val controls = Settings.ControlsEnabled.retrieve(context)
        if (controls) {
            NotificationHelper.pushControls(context)
        }

        // TODO show notification, if enabled
        Log.d("Charging State Change", "power connected")
        Debug.logOverREST(Pair("power_connected", true))
    }

    private fun onPowerDisconnected(context: Context) {
        stopBatteryWatcher(context)

        NotificationHelper.cancelReminder(context)
        NotificationHelper.cancelControls(context)
        stopRinger()
        Log.d("Charging State Change", "power disconnected")
        Debug.logOverREST(Pair("power_connected", false))
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