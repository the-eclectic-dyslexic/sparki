/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.extensions

import android.content.Context
import android.widget.NumberPicker
import com.theeclecticdyslexic.sparki.misc.Settings

fun NumberPicker.init(context: Context, setting: Settings.IntSetting) {
    minValue = 1
    maxValue = 15
    wrapSelectorWheel = false

    value = setting.retrieve(context)

    setOnValueChangedListener {
            _, _, n ->
        setting.store(context, n)
    }
}