package com.theeclecticdyslexic.sparki.background

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.theeclecticdyslexic.sparki.misc.*


class BackgroundService : Service() {

    companion object {
        var running = false
            private set

        fun shouldBeRunning(context: Context): Boolean {
            val enabled = Settings.Enabled.retrieve(context)

            return  enabled && Utils.canComplete(context)
        }

        fun needsToStop(context: Context): Boolean {
            return !shouldBeRunning(context) && running
        }

        fun needsToStart(context: Context): Boolean {
            return shouldBeRunning(context) && !running
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Debug.logOverHTTP(Pair("starting_service", true))

        try {
            if (shouldBeRunning(this)) {
                MainReceiver.start(this)
            }
        } catch (e : Exception) {
            Log.d("Exception Occurred", "While trying to start service $e")
        }

        NotificationHelper.pushSticky(this)

        running = true
        return START_STICKY
    }

    override fun onDestroy() {
        Debug.logOverHTTP(Pair("stopping_service", true))
        MainReceiver.stop(this)

        running = false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}