package com.theeclecticdyslexic.batterychargeassistant.misc

import android.content.Context
import android.content.pm.PackageManager

object Permissions {
    val location  = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_WIFI_STATE)

    fun locationGranted(context: Context):Boolean {
        return location.all {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
    }
}