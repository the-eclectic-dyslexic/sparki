/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.background.delegates

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.theeclecticdyslexic.sparki.misc.Action
import com.theeclecticdyslexic.sparki.misc.NotificationHelper
import com.theeclecticdyslexic.sparki.misc.Settings
import com.theeclecticdyslexic.sparki.misc.Utils

/**
 * Handles the logic related to sending reminder(s) upon charge target being reached
 */

class ReminderSender(context: Context) : BroadcastReceiver(), ChargeTargetReachedDelegate {

    private val manager : AlarmManager
    private val pending : PendingIntent

    override fun delegate(context: Context) {

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

    override fun cancel(context: Context) {
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
        val reminderEnabled = Settings.RemindersEnabled.retrieve(context)
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
        val repeat = Settings.ReminderWillRepeat.retrieve(context)
        if (!repeat) return

        val repeatFrequency = Settings.ReminderInterval.retrieve(context)
        val delta = Utils.minutesToMillis(repeatFrequency)
        val targetTime = System.currentTimeMillis() + delta
        // TODO determine if this is accurate enough through testing
        manager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            targetTime,
            delta,
            pending)
    }
}