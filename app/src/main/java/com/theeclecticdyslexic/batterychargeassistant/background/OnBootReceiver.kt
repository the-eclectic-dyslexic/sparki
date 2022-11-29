package com.theeclecticdyslexic.batterychargeassistant.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val backgroundIntent = Intent(context, BackgroundService::class.java).apply{
            action = BackgroundService::class.java.name
        }

        if (BackgroundService.shouldRun(context)) {
            context.startService(backgroundIntent)
        }
    }
}