package com.theeclecticdyslexic.batterychargeassistant

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
object Utils {
    fun batteryPercentage(context: Context?): Float? {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context?.registerReceiver(null, filter)
        }


        return batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
    }
}