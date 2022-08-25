/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson

/**
 * Contains helper functions for accessing user defined settings,
 * in an attempt to reduce code duplication simply retrieving settings in amongst actual logic
 *
 * Creating new SharedPreferences with every read and write could reduce performance,
 * but given how rarely settings will change it is likely a non-issue
 */

object Settings {

    /*
     * General Settings
     */
    object Enabled: BooleanSetting() {
        override val default = false
    }
    object ControlsEnabled: BooleanSetting() {
        override val default = false
    }
    object UITheme: IntSetting() {
        override val default = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    object ChargeTarget: IntSetting() {
        override val default = 80
    }

    /*
     * Reminder Settings
     */
    object RemindersEnabled: BooleanSetting() {
        override val default = false
    }
    object ReminderWillRepeat: BooleanSetting() {
        override val default = false
    }
    object ReminderInterval: IntSetting() {
        override val default = 1
    }

    /*
     * Alarm Settings
     */
    object AlarmEnabled: BooleanSetting() {
        override val default = false
    }
    object AlarmIgnoresSilent: BooleanSetting() {
        override val default = true
    }
    object AlarmVibrates: BooleanSetting() {
        override val default = false
    }
    object AlarmWillTimeout: BooleanSetting() {
        override val default = true
    }
    object AlarmTimeout: IntSetting() {
        override val default = 2
    }

    /*
     * HTTP GET Request Settings
     */
    object HTTPRequestsEnabled: BooleanSetting() {
        override val default = false
    }
    object HTTPRequestList {
        private val default: String by lazy {
            val defaultArray = arrayOf(HTTPRequest("", ""))
            Gson().toJson(defaultArray)
        }
        fun retrieve(context: Context): Array<HTTPRequest> {
            val json = getPrefs(context).getString(this.javaClass.name, this.default)
            return Gson().fromJson(json, Array<HTTPRequest>::class.java)
        }
        fun store(context: Context, entries: Array<HTTPRequest>) {
            val json = Gson().toJson(entries)

            val editor = getPrefs(context).edit()

            editor.putString(HTTPRequestList.javaClass.name, json)
            editor.apply()
        }
    }

    /*
     * Helper
     */
    private fun getPrefs(context: Context) =
        context.getSharedPreferences(
            Settings.javaClass.name,
            AppCompatActivity.MODE_PRIVATE)

    /*
     * Abstract Classes
     */
    abstract class BooleanSetting {
        protected abstract val default: Boolean

        open fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)

        open fun store(context: Context, setting: Boolean) =
            getPrefs(context).edit()
                .putBoolean(this.javaClass.name, setting)
                .apply()
    }

    abstract class IntSetting {
        protected abstract val default: Int

        open fun retrieve(context: Context) =
            getPrefs(context).getInt(this.javaClass.name, this.default)

        open fun store(context: Context, setting: Int) =
            getPrefs(context).edit()
                .putInt(this.javaClass.name, setting)
                .apply()
    }
}