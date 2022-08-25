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
        val ssid =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getNetworkSSID(context)
            } else {
                deprecated.getNetworkSSID(context)
            }

        val options = if (Permissions.locationGranted(context)) {
            val sanitized = Utils.sanitizeSSID(ssid)
            listOf(sanitized, "")
        } else {
            listOf("")
        }

        val requests = Settings.HTTPRequestList.retrieve(context)
        requests.filter { it.ssid in options }
            .filter { it.url != "" }
            .map { it.url }
            .forEach(Utils::sendHTTPGET)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getNetworkSSID(context: Context): String {
        // TODO there MUST be a better way to do this
        // TODO test on android S device
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        var ssid = ""

        val callback = object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val info = capabilities.transportInfo
                if (info is WifiInfo) {
                    Debug.logOverHTTP("ssid_in_callback", info.ssid)
                } else {
                    Debug.logOverHTTP("transport_info_type", info?.javaClass?.name ?: "unknown")
                }

                ssid = if (info is WifiInfo) info.ssid else ""
            }
        }

        mgr.requestNetwork(request, callback)
        return ssid
    }

    private val deprecated = object {
        fun getNetworkSSID(context: Context): String {
            val mgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return mgr.connectionInfo.ssid
        }
    }
}