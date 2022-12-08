package com.theeclecticdyslexic.sparki.misc

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson

object Settings {

    fun initSharedPreferences(context: Context) {
        val sharedPrefs = getPrefs(context)

        val initialized = sharedPrefs.getBoolean(SettingsInitialized.javaClass.name, false)
        if (initialized) return

        val editor = sharedPrefs.edit()

        editor.putBoolean(SettingsInitialized.javaClass.name, true)

        editor.putBoolean(Enabled.javaClass.name, Enabled.default)
        editor.putBoolean(ControlsEnabled.javaClass.name, ControlsEnabled.default)
        editor.putInt(UITheme.javaClass.name, UITheme.default)
        editor.putInt(ChargeTarget.javaClass.name, ChargeTarget.default)

        editor.putBoolean(ReminderEnabled.javaClass.name, ReminderEnabled.default)
        editor.putInt(ReminderFrequencyMinutes.javaClass.name, ReminderFrequencyMinutes.default)

        editor.putBoolean(AlarmEnabled.javaClass.name, AlarmEnabled.default)
        editor.putBoolean(AlarmIgnoresSilent.javaClass.name, AlarmIgnoresSilent.default)
        editor.putBoolean(AlarmVibrates.javaClass.name, AlarmVibrates.default)
        editor.putInt(AlarmTimeoutMinutes.javaClass.name, AlarmTimeoutMinutes.default)

        editor.putBoolean(HTTPRequestEnabled.javaClass.name, HTTPRequestEnabled.default)
        editor.putString(HTTPRequests.javaClass.name, HTTPRequests.default)

        editor.apply()
    }
    // only used upon first boot
    object SettingsInitialized

    object Enabled {
        const val default = false
        fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)
    }
    object ControlsEnabled {
        const val default = false
        fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)

    }
    object UITheme {
        const val default = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        fun retrieve(context: Context) =
            getPrefs(context).getInt(this.javaClass.name, this.default)
        fun store(context: Context, setting: Int) =
            getPrefs(context).edit()
                .putInt(this.javaClass.name, setting)
                .apply()
    }
    object ChargeTarget {
        const val default = 80
        fun retrieve(context: Context) =
            getPrefs(context).getInt(this.javaClass.name, this.default)

    }
    object ReminderEnabled {
        const val default = true
        fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)

    }
    object ReminderFrequencyMinutes {
        const val default = 0
        fun retrieve(context: Context) =
            getPrefs(context).getInt(this.javaClass.name, this.default)
        fun store(context: Context, setting: Int) =
            getPrefs(context).edit()
                .putInt(this.javaClass.name, setting)
                .apply()
    }
    object AlarmEnabled {
        const val default = false
        fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)

    }
    object AlarmIgnoresSilent {
        const val default = true
        fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)
    }
    object AlarmVibrates {
        const val default = false
        fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)
    }
    object AlarmTimeoutMinutes {
        const val default = 2
        fun retrieve(context: Context) =
            getPrefs(context).getInt(this.javaClass.name, this.default)
        fun store(context: Context, setting: Int) =
            getPrefs(context).edit()
                .putInt(this.javaClass.name, setting)
                .apply()
    }
    object HTTPRequestEnabled {
        const val default = false
        fun retrieve(context: Context) =
            getPrefs(context).getBoolean(this.javaClass.name, this.default)
    }
    object HTTPRequests {
        val default : String by lazy {
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

            editor.putString(HTTPRequests.javaClass.name, json)
            editor.apply()
        }
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(
            Settings.javaClass.name,
            AppCompatActivity.MODE_PRIVATE)

}