package com.theeclecticdyslexic.batterychargeassistant

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import android.widget.Toast
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.jar.Manifest


object Utils {
    private const val HTTP_DEBUGGING = true
    const val BASE_URL = "http://10.0.0.11:8000/"

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

    fun debugHttpRequest(vararg params: Pair<String, Any>) {
        if (!HTTP_DEBUGGING) return

        fun buildRunnable() : Runnable {
            return Runnable {
                val sb = StringBuilder(BASE_URL)
                params.forEach { (label, param) ->
                    sb.append('?')
                        .append(label)
                        .append('=')
                        .append(param)
                }

                val url = URL(sb.toString())

                val connection : HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Android Application:")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1000 * 30
                try {
                    connection.connect()
                } catch(e : Exception) {
                    // do nothing
                    Log.d("exception encounter ", e.toString())
                }
                finally {
                    connection.disconnect()
                }
            }
        }

        val thread = Thread(buildRunnable())
        thread.start()
    }
}
