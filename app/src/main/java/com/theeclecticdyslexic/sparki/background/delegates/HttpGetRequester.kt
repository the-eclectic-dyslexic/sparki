/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.background.delegates

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.theeclecticdyslexic.sparki.misc.*


/**
 * Handles the logic of sending HTTP GET request(s) upon charge target being reached
 */

class HttpGetRequester : ChargeTargetReachedDelegate {

    override fun delegate(context: Context) {
        val httpRequestEnabled = Settings.HTTPRequestsEnabled.retrieve(context)
        if (httpRequestEnabled) sendGETRequests(context)
    }

    override fun cancel(context: Context) {}

    private fun sendGETRequests(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            usingCallback.sendGetRequest(context)
            return
        }

        val options = validSSIDOptions(context)

        val requests = Settings.HTTPRequestList.retrieve(context)
        requests.filter { it.ssid in options }
            .filter { it.url != "" }
            .map { it.url }
            .forEach(Utils::sendHTTPGET)
    }

    private fun validSSIDOptions(context: Context): List<String> {
        val granted = canAskForSSID(context)
        val ssid = if (granted) getSSID(context) else null
        return listOfNotNull(ssid, "")
    }

    private fun getSSID(context: Context): String {
        val raw = deprecated.getNetworkSSID(context)
        return Utils.sanitizeSSID(raw)
    }

    private fun canAskForSSID(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Permissions.backgroundLocationGranted(context)
        } else {
            Permissions.locationGranted(context)
        }

    private val usingCallback = object {

        @RequiresApi(Build.VERSION_CODES.S)
        fun sendGetRequest(context: Context) {
            if (!canAskForSSID(context)){
                return
            }

            val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

            val callback = object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onAvailable(network: Network) {}

                override fun onCapabilitiesChanged(
                    network: Network,
                    capabilities: NetworkCapabilities
                ) {
                    Debug.logOverHTTP("callback")
                    val info = capabilities.transportInfo

                    val raw = if (info is WifiInfo) info.ssid else null
                    val ssid = if (raw != null) Utils.sanitizeSSID(raw) else null
                    val options = listOfNotNull(ssid, "")
                    options.forEach { Debug.logOverHTTP(it) }

                    val requests = Settings.HTTPRequestList.retrieve(context)
                    requests.filter { it.ssid in options }
                        .filter { it.url != "" }
                        .map { it.url }
                        .forEach(Utils::sendHTTPGET)
                    
                    mgr.unregisterNetworkCallback(this)
                }
            }
            mgr.requestNetwork(request, callback)
        }
    }


    private val deprecated = object {
        fun getNetworkSSID(context: Context): String {
            val mgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return mgr.connectionInfo.ssid
        }
    }
}