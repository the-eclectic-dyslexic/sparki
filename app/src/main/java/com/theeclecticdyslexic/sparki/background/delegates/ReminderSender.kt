package com.theeclecticdyslexic.sparki.background.delegates

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.theeclecticdyslexic.sparki.misc.Haltable
import com.theeclecticdyslexic.sparki.misc.Action
import com.theeclecticdyslexic.sparki.misc.NotificationHelper
import com.theeclecticdyslexic.sparki.misc.Settings
import com.theeclecticdyslexic.sparki.misc.Utils

class ReminderSender(context: Context) : BroadcastReceiver(), Haltable {

    private val manager : AlarmManager
    private val pending : PendingIntent

    override fun start(context: Context) {

        try {
            context.registerReceiver(
                this,
                IntentFilter(Action.REPEAT_REMINDER.id)
            )
        } catch (e : Exception) {
            Log.d("failed to register battery change receiver", "$e")
        }

        push(context)
        scheduleRepeatReminder(context)
    }

    override fun stop(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.d("failed to unregister receivers","$e")
        }

        NotificationHelper.cancelReminder(context)
        manager.cancel(pending)
    }

    init {
        manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentRepeat = Intent(Action.REPEAT_REMINDER.id)
        pending = PendingIntent.getBroadcast(
            context,
            0,
            intentRepeat,
            PendingIntent.FLAG_IMMUTABLE)
    }

    private fun push(context: Context) {
        val reminderEnabled = Settings.ReminderEnabled.retrieve(context)
        if (!reminderEnabled) return

        NotificationHelper.pushReminder(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Action.REPEAT_REMINDER.id) {
            Log.d("${javaClass.name} received unhandled intent", action.toString())
            return
        }

        push(context)
    }

    private fun scheduleRepeatReminder(context: Context) {
        val repeatFrequency = Settings.ReminderFrequencyMinutes.retrieve(context)
        if (repeatFrequency <= 0) return

        val delta = Utils.minutesToMillis(repeatFrequency)
        val targetTime = System.currentTimeMillis() + delta
        manager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            targetTime,
            delta,
            pending)
    }
}