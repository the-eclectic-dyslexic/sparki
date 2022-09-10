package com.theeclecticdyslexic.batterychargeassistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val backgroundIntent = Intent(context, BackgroundService::class.java).apply{
            action = BackgroundService::class.java.name
        }

        context.startService(backgroundIntent)


    }
}