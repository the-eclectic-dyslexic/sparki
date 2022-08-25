/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import java.net.HttpURLConnection
import java.net.URL

/**
 * Helper functions that are used in multiple places throughout the project
 */

object Utils {

    fun batteryPercentage(context: Context): Float {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return batteryStatus!!.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
    }

    fun isPlugged(context: Context): Boolean {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val plugStatus = batteryStatus!!.getIntExtra(
            BatteryManager.EXTRA_PLUGGED,
            -1
        )

        return when (plugStatus) {
            BatteryManager.BATTERY_PLUGGED_USB,
            BatteryManager.BATTERY_PLUGGED_AC,
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> true

            else -> false
        }
    }

    fun canComplete(context: Context): Boolean {
        return Settings.HTTPRequestsEnabled.retrieve(context)
                || Settings.RemindersEnabled.retrieve(context)
                || Settings.AlarmEnabled.retrieve(context)
    }

    fun sanitizeSSID(ssid: String): String {
        // source for valid SSID names
        // https://community.cisco.com/t5/wireless-mobility-knowledge-base/characteristics-of-ssids/ta-p/3131765
        val validAnywhere = { c: Char -> c !in "+]/\"\t" }
        val invalidStart = { c: Char -> c in "!#;" }
        val invalidEnd = { c: Char -> c == ' ' }

        return ssid
            .filter(validAnywhere)
            .dropWhile(invalidStart)
            .dropLastWhile(invalidEnd)
    }

    fun minutesToMillis(n: Int): Long {
        val millis = 1000L
        val seconds = 60
        return n * seconds * millis
    }

    fun sendHTTPGET(address: String) {
        val url = URL(address)
        fun buildRunnable(): Runnable {
            return Runnable {
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Android Application:")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1000 * 30
                try {
                    connection.connect()
                    Log.i("response from $url", connection.responseCode.toString())
                } catch (e: Exception) {
                    // do nothing
                    Log.d("exception encountered trying to establish connection ", e.toString())
                } finally {
                    connection.disconnect()
                }
            }
        }

        val thread = Thread(buildRunnable())
        thread.start()
    }
}
