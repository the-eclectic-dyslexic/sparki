package com.theeclecticdyslexic.batterychargeassistant

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.util.*

class BatteryWatcher : BroadcastReceiver() {

    companion object {
        val default = BatteryWatcher()
        private const val BATTERY_MEASUREMENTS = "battery measurements"
        var batteryMeasurements : TreeMap<Long, Float> = TreeMap()
        var lastPercent = -1f

        fun saveBatteryMeasurements(context: Context, batteryMeasurements: TreeMap<Long, Float>) {
            val prefs = context.getSharedPreferences(BATTERY_MEASUREMENTS, Application.MODE_PRIVATE)
            val editor = prefs.edit()

            val toSave = Gson().toJson(batteryMeasurements)
            editor.putString(BATTERY_MEASUREMENTS, toSave)
            editor.apply()
        }

        fun loadBatteryMeasurements(context: Context) : TreeMap<Long, Float> {
            val prefs = context.getSharedPreferences(BATTERY_MEASUREMENTS, Application.MODE_PRIVATE)
            val toDecode = prefs.getString(BATTERY_MEASUREMENTS, "{}")
            return Gson().fromJson(toDecode, TreeMap<Long, Float>().javaClass)
        }
    }

    fun resetBatteryMeasurements(context: Context) {
        lastPercent = -1f
        val prefs = context.getSharedPreferences(BATTERY_MEASUREMENTS, Application.MODE_PRIVATE)
        prefs.edit()
            .clear()
            .apply()
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> onPowerConnected(context)
            Intent.ACTION_POWER_DISCONNECTED -> onPowerDisconnected(context)
            Intent.ACTION_BATTERY_CHANGED -> sendBatteryInfo(context)
            else -> Log.d("battery watcher received unhandled intent","received ${intent.action}")
        }
    }

    private fun sendBatteryInfo(context: Context){
        val percent = Utils.batteryPercentage(context)
        if (lastPercent == percent) return
        lastPercent = percent

        val time = System.currentTimeMillis()


        Utils.debugHttpRequest(Pair("time", time), Pair("percent", percent))
        // TODO check charging or discharging
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
            // don't care, nothing to do, they are already registered
        }
    }

    private fun onPowerConnected(context: Context) {
        val prefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
        val enabled = prefs.getBoolean(Settings.Enabled.javaClass.name, Settings.Enabled.default)
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

    fun stopBatteryWatcher(context: Context) {
        context.unregisterReceiver(this)
        initializeReceivers(context)
    }

    fun startBatteryWatcher(context: Context) {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(this, filter)
        } catch (e : Exception) {
            // do nothing
        }
        resetBatteryMeasurements(context)
    }

}

/*

    private const val MINIMUM_ALARM_TIME = 60*1000
    private const val INITIAL_ALARM_TIME = 5*60*1000

    fun startBatteryWatcher(context: Context) {
        resetBatteryMeasurements(context)
        val measurements = TreeMap<Long, Float>()
        val (time, percent) = getBatteryMeasurement(context)
        // TODO if we are already at charge target, then finish!

        measurements[time] = percent
        saveBatteryMeasurements(context, measurements)

        scheduleFutureAlarm(context, time + INITIAL_ALARM_TIME)
    }

    private fun continueBatteryWatcher(context: Context) {
        val measurements = loadBatteryMeasurements(context)
        val (time, percent) = getBatteryMeasurement(context)
        // TODO if we are already at charge target, then finish!

        measurements[time] = percent
        saveBatteryMeasurements(context, measurements)

        val future = estimateChargeTime(measurements, getChargeTarget(context))

    }

    private fun scheduleFutureAlarm(context: Context, future: Long) {
        val manager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val checkBatteryIntent = Intent(context, this::class.java).apply {
            action = BATTERY_CHECKUP_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, checkBatteryIntent, 0)
        val now = System.currentTimeMillis()
        val targetTime = max(future, now + MINIMUM_ALARM_TIME)
        manager.setExact(AlarmManager.RTC_WAKEUP, targetTime, pendingIntent)
        nextIntent = pendingIntent
    }

    private fun estimateChargeTime(measurements: Map<Long, Float>, chargeTarget: Int): Long {
        val estimates = LinkedList<Long>()
        val list = measurements.toList().sortedBy { it.first }
        for (i in 0 until list.size-1) {
            for (j in i+1 until list.size) {
                estimates.add(makeEstimate(list[i], list[j], chargeTarget))
            }
        }
        val sum = estimates.reduce { acc, l -> acc + l }
        return sum / estimates.size
    }

    private fun makeEstimate(p1: Pair<Long, Float>, p2: Pair<Long, Float>, y3: Int) : Long {
        val rise = p2.second - p1.second
        val run = p2.first - p1.first

        val m = rise / run.toFloat()
        val x1 = p1.first.toFloat()
        val y1 = p1.second
        val b = y1 - x1*m

        val x3 = (y3.toFloat() - b)/m
        return x3.roundToLong()
    }

    private fun getChargeTarget(context: Context) : Int {
        val prefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
        return prefs.getInt(Settings.ChargeTarget.javaClass.name, Settings.ChargeTarget.default)
    }
*/