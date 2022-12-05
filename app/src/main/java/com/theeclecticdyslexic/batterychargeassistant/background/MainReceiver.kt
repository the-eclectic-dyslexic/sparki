package com.theeclecticdyslexic.batterychargeassistant.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.theeclecticdyslexic.batterychargeassistant.background.delegates.AlarmDelegate
import com.theeclecticdyslexic.batterychargeassistant.background.delegates.ChargeWatcherDelegate
import com.theeclecticdyslexic.batterychargeassistant.background.delegates.HTTPRequestDelegate
import com.theeclecticdyslexic.batterychargeassistant.background.delegates.ReminderDelegate
import com.theeclecticdyslexic.batterychargeassistant.misc.*


object MainReceiver : BroadcastReceiver() {

    private lateinit var alarm: AlarmDelegate
    private lateinit var reminder: ReminderDelegate
    private lateinit var getRequester: HTTPRequestDelegate
    private var chargeWatcher: ChargeWatcherDelegate? = null
    val chargeWatcherRunning
        get() = chargeWatcher != null

    private val alwaysReceiving = listOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Action.OVERRIDE_WATCHDOG.id,
        Action.STOP_ALARM.id
    )

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED    -> onPowerConnected(context)
            Intent.ACTION_POWER_DISCONNECTED -> onPowerDisconnected(context)
            Action.OVERRIDE_WATCHDOG.id      -> onOverride(context)
            Action.STOP_ALARM.id             -> alarm.stop()

            // not always being listened to
            Intent.ACTION_BATTERY_CHANGED    -> handleChargePercent(context)

            else -> Log.d("battery watcher received unhandled intent:","${intent.action}")
        }
    }

    fun beginReceiving(context: Context) {
        alarm = AlarmDelegate(context)
        reminder = ReminderDelegate()
        getRequester = HTTPRequestDelegate()

        initBroadcastReceivers(context)
        if (Utils.isPlugged(context)) {
            onPowerConnected(context)
        }
    }

    fun stopReceiving(context: Context) {
        cleanUp(context)
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.d("Exception Occurred", "While trying to destroy service $e")
        }
    }

    private fun handleChargePercent(context: Context) {
        if (chargeWatcher!!.isTargetJustReached(context)) {
            handleFullyCharged(context)
        }
    }

    private fun handleFullyCharged(context: Context) {
        reminder.push(context)
        alarm.play(context)
        getRequester.sendRequests(context)

        NotificationHelper.cancelControls(context)

        stopBatteryWatcher(context)
    }

    private fun initBroadcastReceivers(context: Context) {
        Log.d("registering receivers", "started")
        try {
            for (i in alwaysReceiving) {
                context.applicationContext.registerReceiver(this, IntentFilter(i))
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

        val controlsOn = Settings.ControlsEnabled.retrieve(context)
        if (controlsOn) {
            NotificationHelper.pushControls(context)
        }

        Log.d("Charging State Change", "power connected")
        Debug.logOverHTTP(Pair("power_connected", true))
    }

    private fun onPowerDisconnected(context: Context) {
        cleanUp(context)

        Log.d("Charging State Change", "power disconnected")
        Debug.logOverHTTP(Pair("power_connected", false))
    }

    private fun onOverride(context: Context) {
        cleanUp(context)

        Log.d("Battery Watcher", "overriden")
        Debug.logOverHTTP(Pair("Battery Watcher", "overriden"))
    }

    private fun cleanUp(context: Context) {
        stopBatteryWatcher(context)

        NotificationHelper.cancelControls(context)

        reminder.cancel(context)
        alarm.stop()
    }

    private fun stopBatteryWatcher(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.d("failed to unregister receivers","$e")
        }
        initBroadcastReceivers(context)
        chargeWatcher = null
    }

    private fun startBatteryWatcher(context: Context) {
        try {
            context.registerReceiver(
                this,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (e : Exception) {
            Log.d("failed to register battery change receiver", "$e")
            // do nothing, already registered
        }
        chargeWatcher = ChargeWatcherDelegate(context)
    }

}