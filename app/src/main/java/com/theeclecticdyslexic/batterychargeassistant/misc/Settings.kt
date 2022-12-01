package com.theeclecticdyslexic.batterychargeassistant.misc

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

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
            editor.putInt(ReminderFrequencyMinutes.javaClass.name, ReminderFrequencyMinutes.default)

            editor.putBoolean(AlarmEnabled.javaClass.name, AlarmEnabled.default)
            editor.putBoolean(AlarmIgnoresSilent.javaClass.name, AlarmIgnoresSilent.default)
            editor.putBoolean(AlarmVibrates.javaClass.name, AlarmVibrates.default)
            editor.putInt(AlarmTimeoutMinutes.javaClass.name, AlarmTimeoutMinutes.default)

            editor.putBoolean(HTTPRequestEnabled.javaClass.name, HTTPRequestEnabled.default)
            editor.putString(HTTPRequests.javaClass.name, HTTPRequests.default)

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
            return sharedPrefs.getBoolean(this.javaClass.name, default)
        }
    }
    object ControlsEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, default)
        }
    }
    object ChargeTarget {
        const val default = 80
        fun retrieve(context: Context): Int {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getInt(this.javaClass.name, default)
        }
    }
    object ReminderEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, default)
        }
    }
    object ReminderFrequencyMinutes {
        const val default = 0
        fun retrieve(context: Context): Int {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getInt(this.javaClass.name, default)
        }
    }
    object AlarmEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, default)
        }
    }
    object AlarmIgnoresSilent {
        const val default = true
    }
    object AlarmVibrates {
        const val default = false
    }
    object AlarmTimeoutMinutes {
        const val default = 2
    }
    object HTTPRequestEnabled {
        const val default = false
        fun retrieve(context: Context): Boolean {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            return sharedPrefs.getBoolean(this.javaClass.name, default)
        }
    }
    object HTTPRequests {
        val default : String by lazy {
            val defaultArray = arrayOf(HTTPRequest("", ""))
            Gson().toJson(defaultArray)
        }
        fun retrieve(context: Context): Array<HTTPRequest> {
            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            val json = sharedPrefs.getString(this.javaClass.name, default)!!
            return Gson().fromJson(json, Array<HTTPRequest>::class.java)
        }
        fun store(context: Context, entries: Array<HTTPRequest>) {
            val json = Gson().toJson(entries)

            val sharedPrefs = context.getSharedPreferences(Settings.javaClass.name, AppCompatActivity.MODE_PRIVATE)
            val editor = sharedPrefs.edit()

            editor.putString(HTTPRequests.javaClass.name, json)
            editor.apply()
        }
    }
}