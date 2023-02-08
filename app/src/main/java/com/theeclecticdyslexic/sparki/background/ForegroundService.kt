/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.background

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.theeclecticdyslexic.sparki.misc.*

/**
 * Service used to get around the limitations of being unable to register certain
 * intents with android through the manifest
 *
 * namely:
 * ON_POWER_CONNECTED
 * ON_POWER_DISCONNECTED
 */

class ForegroundService : Service() {

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

        Debug.logOverHTTP("starting_service", true)

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
        Debug.logOverHTTP("stopping_service", true)
        MainReceiver.stop(this)

        running = false
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}