/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

import androidx.appcompat.app.AppCompatDelegate

/**
 * Enum to help with linking strings to Androids theme constants
 */

enum class Themes(val id: Int) {
    System(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    Light(AppCompatDelegate.MODE_NIGHT_NO),
    Dark(AppCompatDelegate.MODE_NIGHT_YES);

    companion object {
        val idMap = values().associateBy { it.id }
    }
}