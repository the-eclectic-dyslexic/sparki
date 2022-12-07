package com.theeclecticdyslexic.sparki.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.theeclecticdyslexic.sparki.misc.Action

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in Action.ON_BOOT_INTENTS) return

        val backgroundIntent = Intent(context, BackgroundService::class.java).apply{
            action = Action.START_BACKGROUND_SERVICE.id
        }

        val needToStart = BackgroundService.needsToStart(context)
        if (needToStart) context.startService(backgroundIntent)
    }
}