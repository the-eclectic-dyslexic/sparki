/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

import android.content.Intent

/**
 * Enum used to define custom actions for intents
 *
 * Also contains a list of actions to do with receiving system notifications on boot
 */
enum class Action {
    CHARGE_TARGET_REACHED,
    OVERRIDE_WATCHDOG,

    STOP_ALARM,
    REPEAT_REMINDER,

    START_FOREGROUND_SERVICE;

    val id = "com.sparki.intent.action.$name"

    companion object {
        const val HTC_QUICKBOOT_POWERON =  "com.htc.intent.action.QUICKBOOT_POWERON"
        const val ANDROID_QUICKBOOT_POWERON =  "android.intent.action.QUICKBOOT_POWERON"

        val ON_BOOT_INTENTS = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            HTC_QUICKBOOT_POWERON,
            ANDROID_QUICKBOOT_POWERON
        )
    }
}
