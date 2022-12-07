package com.theeclecticdyslexic.sparki.misc

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

object Debug {
    private const val REST_LOGGING = true

    // Hardcoded for my local brutally kludged http logging server "python3 -m http.server 8000 2>&1 | tee log.txt"
    // having logging over the local network is the only way I have found to consistently log while testing the app with real world usage
    // If you change this please don't commit and send a pull request for that change without good reason!
    // a good reason might be to make the address something pulled from outside the versioned files but has a default fallback
    private const val BASE_URL = "http://10.0.0.11:8000/"

    fun logOverHTTP(vararg params: Pair<String, Any>) {
        if (!REST_LOGGING) return

        val sb = StringBuilder()
        sb.append(BASE_URL)
        for ((label, param) in params) {
            sb.append("?$label=$param")
        }
        val url = URL(sb.toString())
        fun buildRunnable() : Runnable {
            return Runnable {
                val connection : HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Android Application:")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1000 * 30
                try {
                    connection.connect()
                    Log.d("response from $url", connection.responseCode.toString())
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