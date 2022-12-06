package com.theeclecticdyslexic.batterychargeassistant.misc

import android.content.Context

interface Haltable {
    fun start(context: Context)
    fun stop(context: Context)
}