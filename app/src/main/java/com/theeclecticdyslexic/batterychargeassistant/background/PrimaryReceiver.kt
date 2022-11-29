package com.theeclecticdyslexic.batterychargeassistant.background

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.net.Uri
import android.os.CombinedVibration
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.getSystemService
import com.theeclecticdyslexic.batterychargeassistant.misc.*
import java.util.*


class PrimaryReceiver : BroadcastReceiver() {

    companion object {
        val SINGLETON = PrimaryReceiver()
    }

    private var batteryMeasurements: TreeMap<Long, Float> = TreeMap()
    private var lastPercent = -1f
    private var chargeHandled = false
    private var ringer: PendingIntent? = null

    private val broadcastAlwaysWatched = listOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Action.OVERRIDE_WATCHDOG.id,
        Action.SOUND_ALARM.id,
        Action.STOP_ALARM.id
    )

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
            Action.OVERRIDE_WATCHDOG.id -> SINGLETON.onPowerDisconnected(context)

            Intent.ACTION_BATTERY_CHANGED -> SINGLETON.handleChargePercent(context)

            Action.SOUND_ALARM.id -> makeNoise(context)

            else -> Log.d("battery watcher received unhandled intent:","${intent.action}")
        }
    }

    private fun makeNoise(context: Context) {

        Debug.logOverREST(Pair("alarm", "starting"))
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val r = RingtoneManager.getRingtone(context, alarmUri)

        r.play()
    }

    private fun stopNoise(context: Context) {
        if (ringer == null) return

        Debug.logOverREST(Pair("alarm", "stopping"))
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(ringer)
        ringer = null
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
            soundAlarm(context)
        }

        val httpRequestEnabled = Settings.HTTPRequestEnabled.retrieve(context)
        if (httpRequestEnabled) {
            // TODO send httpRequest
            sendHTTPRequests()
        }

        NotificationHelper.cancelControls(context)

        stopBatteryWatcher(context)
    }

    // TODO merge notification controls with alarm
    private fun soundAlarm(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(
            context,
            PrimaryReceiver::class.java).apply {
            action = Action.SOUND_ALARM.id
            flags = Intent.FLAG_RECEIVER_FOREGROUND or
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME
        }
        ringer = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE)

        alarm.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 1, // run 1ms from now
            ringer
        )
    }

    private fun sendHTTPRequests() {
        Debug.logOverREST(Pair("plug_power", "off"))
    }

    private fun initBroadcastReceivers(context: Context) {
        Log.d("registering receivers", "started")
        try {
            val intents = broadcastAlwaysWatched
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
        stopNoise(context)
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