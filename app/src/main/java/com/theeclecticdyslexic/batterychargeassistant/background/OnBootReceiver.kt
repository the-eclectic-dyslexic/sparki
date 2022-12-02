package com.theeclecticdyslexic.batterychargeassistant.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.theeclecticdyslexic.batterychargeassistant.misc.Action
import com.theeclecticdyslexic.batterychargeassistant.misc.Debug

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in Action.ON_BOOT_INTENTS) return

        val backgroundIntent = Intent(context, BackgroundService::class.java).apply{
            action = Action.START_BACKGROUND_SERVICE.id
        }

        val needToStart = BackgroundService.shouldRun(context)
        if (needToStart) context.startService(backgroundIntent)
    }
}