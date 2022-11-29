package com.theeclecticdyslexic.batterychargeassistant.background

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.theeclecticdyslexic.batterychargeassistant.misc.Debug
import com.theeclecticdyslexic.batterychargeassistant.misc.NotificationHelper
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings
import com.theeclecticdyslexic.batterychargeassistant.misc.Utils


class BackgroundService : Service() {

    companion object {
        var running = false
            private set

        fun shouldRun(context: Context): Boolean {
            val enabled = Settings.Enabled.retrieve(context)

            return  enabled && Utils.canComplete(context)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Debug.logOverREST(Pair("starting_service", true))

        val enabled = Settings.Enabled.retrieve(this)
        try {
            if (enabled) {
                MainReceiver.SINGLETON.beginReceiving(this)
            }
        } catch (e : Exception) {
            Log.d("Exception Occurred", "While trying to start service $e")
        }

        NotificationHelper.pushSticky(this)

        running = true
        return START_STICKY
    }

    override fun onDestroy() {
        Debug.logOverREST(Pair("stopping_service", true))
        MainReceiver.SINGLETON.stopReceiving(this)

        running = false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}