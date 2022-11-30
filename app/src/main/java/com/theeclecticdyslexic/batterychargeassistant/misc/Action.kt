package com.theeclecticdyslexic.batterychargeassistant.misc

enum class Action {
    STOP_ALARM,
    START_BACKGROUND_SERVICE,
    OVERRIDE_WATCHDOG;

    val id = "custom.intent.action.$name"
}
