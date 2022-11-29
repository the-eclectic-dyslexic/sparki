package com.theeclecticdyslexic.batterychargeassistant.misc

enum class Action {
    STOP_ALARM,
    OVERRIDE_WATCHDOG;

    val id = "custom.intent.action.$name"
}
