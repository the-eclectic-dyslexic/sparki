package com.theeclecticdyslexic.batterychargeassistant.background.delegates

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
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.theeclecticdyslexic.batterychargeassistant.misc.Haltable
import com.theeclecticdyslexic.batterychargeassistant.misc.*

class AlarmSounder(context: Context) : BroadcastReceiver(), Haltable {
    private val ringer: Ringtone
    private val vibrator: Vibrator
    private val manager: AlarmManager
    private val pending: PendingIntent

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Action.STOP_ALARM.id) {
            Log.d("${javaClass.name} received unhandled intent", action.toString())
            return
        }

        this.stop(context)
    }

    override fun start(context: Context) {
        val alarmEnabled = Settings.AlarmEnabled.retrieve(context)
        if (!alarmEnabled) return

        Debug.logOverHTTP(Pair("alarm", "starting"))

        val ring = shouldRing(context)
        if (ring) {
            ringer.play()
        }

        val vibrate = shouldVibrate(context)
        if (vibrate) {
            startVibration()
        }

        if (ring || vibrate) {
            initReceiver(context)
            scheduleTimeout(context)
            NotificationHelper.pushReminder(context)
        }
    }

    override fun stop(context: Context) {
        Debug.logOverHTTP(Pair("alarm", "stopping"))

        destroyReceiver(context)

        ringer.stop()
        vibrator.cancel()
        manager.cancel(pending)
    }

    private fun initReceiver(context: Context) {
        try {
            context.registerReceiver(
                this,
                IntentFilter(Action.STOP_ALARM.id)
            )
        } catch (e : Exception) {
            Log.d("failed to register battery change receiver", "$e")
        }
    }

    private fun destroyReceiver(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.d("failed to unregister receivers","$e")
        }
    }

    init {
        ringer   = initRinger(context)
        vibrator = initVibrator(context)
        manager  = initManager(context)
        pending  = initPending(context)
    }

    private fun initRinger(context: Context): Ringtone{
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringer = RingtoneManager.getRingtone(context, alarmUri)
        ringer.audioAttributes =
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build()

        return ringer
    }

    private fun initVibrator(context: Context): Vibrator {
        // TODO implement for android 12+ when a device can be tested on
        return context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun initManager(context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun initPending(context: Context): PendingIntent {
        val intentStopRinging = Intent(Action.STOP_ALARM.id)
        return PendingIntent.getBroadcast(
            context,
            0,
            intentStopRinging,
            PendingIntent.FLAG_IMMUTABLE)
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
            0)
        )
    }

    private fun scheduleTimeout(context: Context) {
        val timeout = Settings.AlarmTimeoutMinutes.retrieve(context)
        Log.d("timeout", timeout.toString())
        if (timeout <= 0) return

        val delta = Utils.minutesToMillis(timeout)
        val targetTime = System.currentTimeMillis() + delta
        manager.setExact(
            AlarmManager.RTC_WAKEUP,
            targetTime,
            pending)
    }
}