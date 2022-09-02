package com.theeclecticdyslexic.batterychargeassistant

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class NotificationHandler: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val manager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(MainActivity.NOTIFICATION_ID)
    }
}