/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.extensions

import android.content.Context
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import com.theeclecticdyslexic.sparki.misc.Settings

fun SwitchCompat.init(context: Context,
                      setting: Settings.BooleanSetting,
                      listener: CompoundButton.OnCheckedChangeListener? = null) {
    this.isChecked = setting.retrieve(context)

    val defaultListener =
        CompoundButton.OnCheckedChangeListener {
                _, isChecked -> setting.store(context, isChecked)
        }
    this.setOnCheckedChangeListener(listener ?: defaultListener)
}