package com.theeclecticdyslexic.batterychargeassistant.background.delegates

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.theeclecticdyslexic.batterychargeassistant.misc.Settings
import com.theeclecticdyslexic.batterychargeassistant.misc.Utils

class HTTPRequestDelegate {

    fun sendRequests(context: Context) {
        val httpRequestEnabled = Settings.HTTPRequestEnabled.retrieve(context)
        if (httpRequestEnabled) sendHTTPRequests(context)
    }

    private fun sendHTTPRequests(context: Context) {
        val ssid =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getNetworkSSID(context)
            else deprecatedGetNetworkSSID(context)

        Log.d("checking if ssid is assigned synchronously", ssid)

        val sanitized = Utils.sanitizeSSID(ssid)
        val options = listOf(sanitized, "")
        val requests = Settings.HTTPRequests.retrieve(context)

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
                Log.d("ssid in callback", (capabilities.transportInfo as WifiInfo).ssid)

                val info = capabilities.transportInfo
                ssid = if (info is WifiInfo) info.ssid else ""
            }
        }

        mgr.requestNetwork(request, callback)
        return ssid
    }

    private fun deprecatedGetNetworkSSID(context: Context): String {
        val deprecatedMgr = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return deprecatedMgr.connectionInfo.ssid
    }
}