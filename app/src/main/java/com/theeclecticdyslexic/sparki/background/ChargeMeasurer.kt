/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.theeclecticdyslexic.sparki.misc.*
import java.util.*

/**
 * Watches for changes in battery charge progress
 * When charge target is reached, this class will inform the "MainReceiver"
 */

class ChargeMeasurer : BroadcastReceiver() {
    private var batteryMeasurements: TreeMap<Long, Float> = TreeMap()
    private var lastPercent = -1f
    private var chargeHandled = false
    var running = false
        private set

    fun start(context: Context) {
        running = true
        resetMemberVariables(context)

        try {
            context.registerReceiver(
                this,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        } catch (e : Exception) {
            Log.d("failed to register battery change receiver", "$e")
        }

        val controlsOn = Settings.ControlsEnabled.retrieve(context)
        if (controlsOn) NotificationHelper.pushControls(context)
    }

    fun stop(context: Context) {
        running = false

        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.d("failed to unregister receivers","$e")
        }

        NotificationHelper.cancelControls(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BATTERY_CHANGED) {
            Log.d("${javaClass.name} received unhandled intent", action.toString())
            return
        }

        onBatteryChanged(context)
    }

    private fun onBatteryChanged(context: Context) {
        val percent = Utils.batteryPercentage(context)
        addMeasurement(percent)

        val target = Settings.ChargeTarget.retrieve(context)
        val targetMet = target <= percent

        if (targetMet && !chargeHandled) informMainReceiver(context)
    }

    private fun informMainReceiver(context: Context) {
        chargeHandled = true
        val intent = Intent(Action.CHARGE_TARGET_REACHED.id)
        context.sendBroadcast(intent)
    }

    private fun resetMemberVariables(context: Context) {
        chargeHandled = false
        batteryMeasurements = TreeMap()
        lastPercent = Utils.batteryPercentage(context)
    }

    private fun addMeasurement(percent: Float) {
        if (lastPercent == percent) return
        lastPercent = percent
        val time = System.currentTimeMillis()
        batteryMeasurements[time] = percent
        Debug.logOverHTTP(Pair("time", time), Pair("percent", percent))
    }
}