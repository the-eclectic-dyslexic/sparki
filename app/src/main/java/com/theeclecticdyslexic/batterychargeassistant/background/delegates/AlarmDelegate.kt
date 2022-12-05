package com.theeclecticdyslexic.batterychargeassistant.background.delegates

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.theeclecticdyslexic.batterychargeassistant.misc.Action
import com.theeclecticdyslexic.batterychargeassistant.misc.Debug
import com.theeclecticdyslexic.batterychargeassistant.misc.NotificationHelper
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings

class AlarmDelegate(context: Context) {
    private lateinit var ringer: Ringtone
    private lateinit var vibrator: Vibrator

    init {
        initRinger(context)
        initVibrator(context)
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

    fun play(context: Context) {
        val alarmEnabled = Settings.AlarmEnabled.retrieve(context)
        if (!alarmEnabled) return

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

    fun stop() {
        Debug.logOverHTTP(Pair("alarm", "stopping"))
        ringer.stop()
        vibrator.cancel()
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

        vibrator.vibrate(
            VibrationEffect.createWaveform(
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
}