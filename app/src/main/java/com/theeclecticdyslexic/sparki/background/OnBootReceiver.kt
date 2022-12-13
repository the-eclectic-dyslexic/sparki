/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.theeclecticdyslexic.sparki.misc.Action

/**
 * Receiver registered with the system to start the "foreground service" Sparki relies on
 * upon restarting the device, so that the user doesn't need to reopen the application again
 * before starting to charge their device.
 */
class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in Action.ON_BOOT_INTENTS) return

        val backgroundIntent = Intent(context, ForegroundService::class.java).apply{
            action = Action.START_FOREGROUND_SERVICE.id
        }

        val needToStart = ForegroundService.needsToStart(context)
        if (needToStart) context.startForegroundService(backgroundIntent)
    }
}