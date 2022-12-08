package com.theeclecticdyslexic.sparki.misc

import androidx.appcompat.app.AppCompatDelegate

enum class Themes(val id: Int) {
    System(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    Light(AppCompatDelegate.MODE_NIGHT_NO),
    Dark(AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        val idMap = values().associateBy { it.id }
    }
}