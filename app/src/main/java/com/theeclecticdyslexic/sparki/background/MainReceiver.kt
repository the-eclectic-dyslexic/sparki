package com.theeclecticdyslexic.sparki.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.theeclecticdyslexic.sparki.background.delegates.AlarmSounder
import com.theeclecticdyslexic.sparki.background.delegates.ChargeMeasurer
import com.theeclecticdyslexic.sparki.background.delegates.GETRequester
import com.theeclecticdyslexic.sparki.background.delegates.ReminderSender
import com.theeclecticdyslexic.sparki.misc.*


object MainReceiver : BroadcastReceiver() {

    private lateinit var reminderSender: ReminderSender
    private lateinit var alarmSounder: AlarmSounder
    private val getRequester = GETRequester()

    private val chargeMeasurer = ChargeMeasurer()
    val chargeReceiverRunning
        get() = chargeMeasurer.running

    private val onChargeTargetReachedDelegates by lazy {
        listOf(
            reminderSender,
            alarmSounder,
            getRequester)
    }

    private val intentMap = mapOf(
        Pair( Intent.ACTION_POWER_CONNECTED,    ::onPowerConnected),
        Pair( Intent.ACTION_POWER_DISCONNECTED, ::onPowerDisconnected),
        Pair( Action.OVERRIDE_WATCHDOG.id,      ::onOverrideWatchdog),
        Pair( Action.CHARGE_TARGET_REACHED.id,  ::onChargeTargetReached)
    )

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val handler = intentMap[action]
        if (handler == null) {
            Log.d("${javaClass.name} received unhandled intent", action.toString())
            return
        }

        // used instead of when statement to avoid mistakes in code duplication
        handler.invoke(context)
    }

    fun start(context: Context) {
        alarmSounder = AlarmSounder(context)
        reminderSender = ReminderSender(context)
        initBroadcastReceivers(context)

        if (Utils.isPlugged(context)) {
            onPowerConnected(context)
        }
    }

    fun stop(context: Context) {
        cleanUp(context)
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.d("Exception Occurred", "While trying to destroy service $e")
        }
    }

    private fun onChargeTargetReached(context: Context) {
        onChargeTargetReachedDelegates.forEach { it.start(context) }

        chargeMeasurer.stop(context)
    }

    private fun initBroadcastReceivers(context: Context) {
        Log.d("registering receivers", "started")
        try {
            for (intentName in intentMap.keys) {
                context.applicationContext.registerReceiver(this, IntentFilter(intentName))
            }

            Log.d("registering receivers", "completed")
        } catch (e: Exception) {
            Log.d("failed to register receiver", "$e")
        }
    }

    private fun onPowerConnected(context: Context) {
        val enabled = Settings.Enabled.retrieve(context)
        if (!enabled) return // shouldn't be necessary

        chargeMeasurer.start(context)

        Log.d("Charging State Change", "power connected")
        Debug.logOverHTTP(Pair("power_connected", true))
    }

    private fun onPowerDisconnected(context: Context) {
        cleanUp(context)

        Log.d("Charging State Change", "power disconnected")
        Debug.logOverHTTP(Pair("power_connected", false))
    }

    private fun onOverrideWatchdog(context: Context) {
        cleanUp(context)

        Log.d("Battery Watcher", "overriden")
        Debug.logOverHTTP(Pair("battery_watcher", "overriden"))
    }

    private fun cleanUp(context: Context) {
        onChargeTargetReachedDelegates.forEach { it.stop(context) }
    }
}