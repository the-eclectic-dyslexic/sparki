/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Helpers specific to granting and checking runtime permissions
 */

typealias PermissionSet = Array<String>


object Permissions {
    @RequiresApi(Build.VERSION_CODES.Q)
    val backgroundLocation : PermissionSet =
        arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val location : PermissionSet =
        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val post : PermissionSet = arrayOf(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    const val REQUEST_LOCATION_AND_WIFI_PERMISSION = 1
    const val REQUEST_POST_PERMISSION = 2
    const val REQUEST_BACKGROUND_LOCATION_WORKAROUND = 3

    fun postGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || post.granted(context)

    fun locationGranted(context: Context) = location.granted(context)

    @RequiresApi(Build.VERSION_CODES.Q)
    fun backgroundLocationGranted(context: Context) = (location + backgroundLocation).granted(context)

    fun onlyForegroundGranted(context: Context): Boolean {
        val location = locationGranted(context)
        val background =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    backgroundLocationGranted(context)
        return location && !background
    }

    private fun PermissionSet.granted(context: Context): Boolean =
        this.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
}