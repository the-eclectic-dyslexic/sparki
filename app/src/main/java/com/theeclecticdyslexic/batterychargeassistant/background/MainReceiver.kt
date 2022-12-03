package com.theeclecticdyslexic.batterychargeassistant.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi
import com.theeclecticdyslexic.batterychargeassistant.misc.*
import java.util.*


object MainReceiver : BroadcastReceiver() {

    private var batteryMeasurements: TreeMap<Long, Float> = TreeMap()
    private var lastPercent = -1f
    private var chargeHandled = false
    private lateinit var ringer: Ringtone
    private lateinit var vibrator: Vibrator
    var batteryWatcherRunning = true
        private set

    private val alwaysReceiving = listOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Action.OVERRIDE_WATCHDOG.id,
        Action.STOP_ALARM.id
    )

    fun beginReceiving(context: Context) {
        initRinger(context)
        initVibrator(context)
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

    private fun resetBatteryMeasurements(context: Context) {
        batteryMeasurements = TreeMap()
        lastPercent = Utils.batteryPercentage(context)
        chargeHandled = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED    -> onPowerConnected(context)
            Intent.ACTION_POWER_DISCONNECTED -> onPowerDisconnected(context)
            Action.OVERRIDE_WATCHDOG.id      -> onOverride(context)
            Action.STOP_ALARM.id             -> stopRinger()

            // not always being listened to
            Intent.ACTION_BATTERY_CHANGED    -> handleChargePercent(context)

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

    private fun initVibrator(context: Context) {
        // TODO implement for android 12+ when a device can be tested on
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun playRinger(context: Context) {
        Debug.logOverHTTP(Pair("alarm", "starting"))
        initRinger(context)

        val ring = shouldRing(context)
        if (ring) {
            ringer.play()
        }

        val vibrate = shouldVibrate(context)
        if (vibrate) {
            startVibration()
        }

        if (ring || vibrate) {
            scheduleTimeout(context)
            NotificationHelper.pushReminder(context)
        }
    }

    private fun shouldRing(context: Context): Boolean {
        val mgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val volumeOn = mgr.ringerMode == AudioManager.RINGER_MODE_NORMAL
        val ignoreSilent = Settings.AlarmIgnoresSilent.retrieve(context)

        return ignoreSilent || volumeOn
    }

    private fun shouldVibrate(context: Context): Boolean {
        val mgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val notSilent = mgr.ringerMode != AudioManager.RINGER_MODE_SILENT
        val ignoreSilent = Settings.AlarmIgnoresSilent.retrieve(context)
        val vibrationEnabled = Settings.AlarmVibrates.retrieve(context)

        return vibrationEnabled && (notSilent || ignoreSilent)
    }

    private fun startVibration() {
        val def = VibrationEffect.DEFAULT_AMPLITUDE
        val timings    = longArrayOf(500L, 500L, 500L, 1500L)
        val amplitudes = intArrayOf (def,  0,    def,  0)

        vibrator.vibrate(VibrationEffect.createWaveform(
            timings,
            amplitudes,
            0
        ))
    }

    private fun scheduleTimeout(context: Context) {
        val timeout = Settings.AlarmTimeoutMinutes.retrieve(context)
        Log.d("timeout", timeout.toString())
        if (timeout <= 0) return

        val intentStopRinging = Intent(Action.STOP_ALARM.id)
        val pi = PendingIntent.getBroadcast(
            context,
            0,
            intentStopRinging,
            PendingIntent.FLAG_IMMUTABLE)

        val targetTime = calculateTimeout(timeout)
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.setExact(
            AlarmManager.RTC_WAKEUP,
            targetTime,
            pi)
    }

    private fun calculateTimeout(timeout: Int): Long {
        val millis = 1000
        val seconds = 60
        val delta = timeout * seconds * millis
        return System.currentTimeMillis() + delta
    }

    private fun stopRinger() {
        Debug.logOverHTTP(Pair("alarm", "stopping"))
        ringer.stop()
        vibrator.cancel()
    }

    private fun handleChargePercent(context: Context) {

        val percent = Utils.batteryPercentage(context)
        logMeasurement(percent)

        val target = Settings.ChargeTarget.retrieve(context)
        val targetMet = target <= percent

        when {
            targetMet && !chargeHandled -> handleFullyCharged(context)
            !targetMet                  -> chargeHandled = false
        }
    }

    private fun logMeasurement(percent: Float) {
        if (lastPercent == percent) return
        lastPercent = percent
        val time = System.currentTimeMillis()
        batteryMeasurements[time] = percent
        Debug.logOverHTTP(Pair("time", time), Pair("percent", percent))
    }

    private fun handleFullyCharged(context: Context) {
        chargeHandled = true

        val reminderEnabled = Settings.ReminderEnabled.retrieve(context)
        if (reminderEnabled) NotificationHelper.pushReminder(context)

        val alarmEnabled = Settings.AlarmEnabled.retrieve(context)
        if (alarmEnabled) playRinger(context)

        val httpRequestEnabled = Settings.HTTPRequestEnabled.retrieve(context)
        if (httpRequestEnabled) sendHTTPRequests(context)

        NotificationHelper.cancelControls(context)

        stopBatteryWatcher(context)
    }

    private fun sendHTTPRequests(context: Context) {
        val ssid =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getNetworkSSID(context)
            else deprecatedGetNetworkSSID(context)

        Log.d("checking if ssid is assigned synchronously", ssid)

        val sanitized = Utils.sanitizeSSID(ssid)
        val options = listOf(sanitized, "")
        val requests = Settings.HTTPRequests.retrieve(context)

        requests.filter { it.ssid in options }
                .filter { it.url != "" }
                .map { it.url }
                .forEach(Utils::sendREST)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getNetworkSSID(context: Context): String {
        // TODO there MUST be a better way to do this
        // TODO test on android S device
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        var ssid = ""

        val callback = object : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                Log.d("ssid in callback", (capabilities.transportInfo as WifiInfo).ssid)

                val info = capabilities.transportInfo
                ssid = if (info is WifiInfo) info.ssid else ""
            }
        }

        mgr.requestNetwork(request, callback)
        return ssid
    }

    private fun deprecatedGetNetworkSSID(context: Context): String {
        val deprecatedMgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return deprecatedMgr.connectionInfo.ssid
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

        val controls = Settings.ControlsEnabled.retrieve(context)
        if (controls) {
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

        NotificationHelper.cancelReminder(context)
        NotificationHelper.cancelControls(context)
        stopRinger()
    }

    private fun stopBatteryWatcher(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.d("failed to unregister receivers","$e")
        }
        initBroadcastReceivers(context)
        batteryWatcherRunning = false
    }

    private fun startBatteryWatcher(context: Context) {
        try {
            context.registerReceiver(
                this,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            resetBatteryMeasurements(context)
        } catch (e : Exception) {
            // do nothing, already registered
        }
        batteryWatcherRunning = true
    }

}