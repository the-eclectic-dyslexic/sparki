/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

import android.util.Log
import com.theeclecticdyslexic.sparki.extensions.head
import com.theeclecticdyslexic.sparki.extensions.tail
import java.net.HttpURLConnection
import java.net.URL

/**
 * Used only during development
 * Intended purely for debugging purposes
 *
 * Unfortunately, this application is somewhat difficult to test, given its nature as something
 * that need to run while the device charges. This necessitates being able to get logs both when the
 * application is receiving power and not receiving power. This is at odds with usb-debugging.
 *
 * Wireless debugging could be used, but on some devices it is not very reliable, and will stop sending
 * debugging information regularly. This necessitates a different way of getting logs from the app
 * while it runs. The solution I (The Eclectic Dyslexic) came up with is sending http requests
 * with the debug information needed in the get parameters. This allows continuously logging
 * what is happening on the device throughout a day, regardless of if wireless debugging is still
 * running. Meaning, only an http server needs to be running, and the device can be used normally
 * without having to pay attention to the sometimes tenuous connection between the device and IDE.
 */

typealias GetParam = Pair<String, Any>

object Debug {

    // Hardcoded for my local brutally kludged http logging server "python3 -m http.server 8000 2>&1 | tee log.txt"
    // having logging over the local network is the only way I have found to consistently log while testing the app with real world usage
    // If you change this please don't commit and send a pull request for that change without good reason!
    // a good reason might be to make the address something pulled from outside the versioned files but has a default fallback
    private const val SERVER_IP_AND_PORT = "10.0.0.175:8000"
    private const val BASE_URL = "http://$SERVER_IP_AND_PORT/"
    private const val REST_LOGGING = false


    fun logOverHTTP(label: String, param: Any = "") = logOverHTTP(Pair(label, param))

    fun logOverHTTP(vararg params: GetParam) {
        if (!REST_LOGGING) return

        val saneParams = params.map { it.replaceSpaces() }
        val url = buildUrl(saneParams)

        val thread = buildRequestThread(url)
        thread.start()
    }

    private fun buildRequestThread(url: URL) : Thread {
        val runnable = Runnable {
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Android Application:")
            connection.setRequestProperty("Connection", "close")
            connection.connectTimeout = 1000 * 30
            try {
                connection.connect()
                Log.d("response from $url", connection.responseCode.toString())
            } catch(e : Exception) {
                Log.d("exception encounter ", e.toString())
            }
            finally {
                connection.disconnect()
            }
        }
        return Thread(runnable)
    }

    private fun buildUrl(params: List<GetParam>): URL {
        if (params.isEmpty()) {
            return URL(BASE_URL)
        }

        val sb = StringBuilder()
        sb.append(BASE_URL)

        params.head.let {
            (label, value) -> sb.append("?$label=$value")
        }
        params.tail.forEach {
            (label, value) -> sb.append("&$label=$value")
        }

        return URL(sb.toString())
    }

    private fun GetParam.replaceSpaces(): GetParam {
        val (label, value) = this
        return GetParam(
            label.replaceSpaces(),
            value.toString().replaceSpaces()
        )
    }

    private fun String.replaceSpaces(): String =
        this.replace(' ', '_')
}
