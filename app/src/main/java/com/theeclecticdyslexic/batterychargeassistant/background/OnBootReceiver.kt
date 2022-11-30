package com.theeclecticdyslexic.batterychargeassistant.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.theeclecticdyslexic.batterychargeassistant.misc.Action

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val backgroundIntent = Intent(context, BackgroundService::class.java).apply{
            action = Action.START_BACKGROUND_SERVICE.id
        }

        val needToStart = !BackgroundService.running && BackgroundService.shouldRun(context)
        if (needToStart) context.startService(backgroundIntent)
    }
}