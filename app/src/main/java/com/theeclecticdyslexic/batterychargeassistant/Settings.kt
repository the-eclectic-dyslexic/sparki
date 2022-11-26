package com.theeclecticdyslexic.batterychargeassistant

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object Settings {

    fun initSharedPreferences(context: Context) {
        val sharedPrefs = context.getSharedPreferences(javaClass.name, AppCompatActivity.MODE_PRIVATE)

        // true is used instead of the default value here because if the app has never been opened
        // (the key for "ResetDefaults" is not present) we want to set all the settings to default
        if (sharedPrefs.getBoolean(ResetDefaults.javaClass.name, true)) {
            val editor = sharedPrefs.edit()

            editor.putBoolean(ResetDefaults.javaClass.name, ResetDefaults.default)

            editor.putBoolean(Enabled.javaClass.name, Enabled.default)
            editor.putBoolean(ControlsEnabled.javaClass.name, ControlsEnabled.default)
            editor.putInt(ChargeTarget.javaClass.name, ChargeTarget.default)

            editor.putBoolean(ReminderEnabled.javaClass.name, ReminderEnabled.default)
            editor.putInt(ReminderFrequency.javaClass.name, ReminderFrequency.default)

            editor.putBoolean(AlarmEnabled.javaClass.name, AlarmEnabled.default)

            editor.putBoolean(HTTPRequestEnabled.javaClass.name, HTTPRequestEnabled.default)
            editor.putString(HTTPRequestURL.javaClass.name, HTTPRequestURL.default)
            editor.putString(WhiteListedSSIDs.javaClass.name, WhiteListedSSIDs.default)

            editor.apply()
        }
    }
    // only used upon first boot and to reset settings
    object ResetDefaults {
        const val default = false
    }

    object Enabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, this.default)
        }
    }
    object ControlsEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, this.default)
        }
    }
    object ChargeTarget {
        const val default = 80
        fun retrieve(context: Context): Int {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getInt(this.javaClass.name, this.default)
        }
    }
    object ReminderEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, this.default)
        }
    }
    object ReminderFrequency {
        const val default = 0
        fun retrieve(context: Context): Int {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getInt(this.javaClass.name, this.default)
        }
    }
    object AlarmEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, this.default)
        }
    }
    object HTTPRequestEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, this.default)
        }
    }
    object HTTPRequestURL {
        const val default = ""
        fun retrieve(context: Context): String {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getString(this.javaClass.name, this.default)!!
        }
    }
    object WhiteListedSSIDs {
        const val default = ""
        fun retrieve(context: Context): String {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getString(this.javaClass.name, this.default)!!
        }
    }
}