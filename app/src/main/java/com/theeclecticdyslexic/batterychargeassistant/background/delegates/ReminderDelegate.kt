package com.theeclecticdyslexic.batterychargeassistant.background.delegates

import android.content.Context
import com.theeclecticdyslexic.batterychargeassistant.misc.NotificationHelper
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings

class ReminderDelegate {

    fun push(context: Context) {
        val reminderEnabled = Settings.ReminderEnabled.retrieve(context)
        if (reminderEnabled) NotificationHelper.pushReminder(context)
    }

    fun cancel(context: Context) {
        NotificationHelper.cancelReminder(context)
    }
}