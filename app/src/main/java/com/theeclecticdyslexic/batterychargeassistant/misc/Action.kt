package com.theeclecticdyslexic.batterychargeassistant.misc

import android.content.Intent

enum class Action {
    STOP_ALARM,
    START_BACKGROUND_SERVICE,
    CHARGE_TARGET_REACHED,
    OVERRIDE_WATCHDOG,
    REPEAT_REMINDER;

    val id = "custom.intent.action.$name"

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
