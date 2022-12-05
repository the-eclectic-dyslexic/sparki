package com.theeclecticdyslexic.batterychargeassistant.background.delegates

import android.content.Context
import com.theeclecticdyslexic.batterychargeassistant.misc.Debug
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings
import com.theeclecticdyslexic.batterychargeassistant.misc.Utils
import java.util.*

class ChargeWatcherDelegate(context: Context) {
    private var batteryMeasurements: TreeMap<Long, Float> = TreeMap()
    private var lastPercent = -1f
    private var chargeHandled = false

    init {
        lastPercent = Utils.batteryPercentage(context)
    }

    private fun addMeasurement(percent: Float) {
        if (lastPercent == percent) return
        lastPercent = percent
        val time = System.currentTimeMillis()
        batteryMeasurements[time] = percent
        Debug.logOverHTTP(Pair("time", time), Pair("percent", percent))
    }

    fun isTargetJustReached(context: Context): Boolean {
        val percent = Utils.batteryPercentage(context)
        addMeasurement(percent)

        val target = Settings.ChargeTarget.retrieve(context)
        val targetMet = target <= percent

        return when {
            targetMet && !chargeHandled -> true
            !targetMet -> {
                chargeHandled = false
                false
            }
            else -> false
        }
    }
}