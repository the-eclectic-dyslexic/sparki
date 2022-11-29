package com.theeclecticdyslexic.batterychargeassistant.misc

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL


object Utils {

    fun batteryPercentage(context: Context): Float {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return batteryStatus!!.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
    }

    fun isPlugged(context: Context): Boolean {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val plugStatus = batteryStatus!!.getIntExtra(
            BatteryManager.EXTRA_PLUGGED,
            -1)

        return when(plugStatus) {
            BatteryManager.BATTERY_PLUGGED_USB,
            BatteryManager.BATTERY_PLUGGED_AC,
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> true

            else -> false
        }
    }

    fun canComplete(context: Context): Boolean {
        return     Settings.HTTPRequestEnabled.retrieve(context)
                || Settings.ReminderEnabled.retrieve(context)
                || Settings.AlarmEnabled.retrieve(context)
    }

}
